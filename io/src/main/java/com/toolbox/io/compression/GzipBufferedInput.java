package com.toolbox.io.compression;

import com.toolbox.io.buffer.BufferedInput;
import com.toolbox.io.buffer.WritableBuffer;
import com.toolbox.io.spi.FilteringBufferedInput;
import com.toolbox.io.spi.TracingBufferedInput;
import com.toolbox.io.util.InflaterUtil;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

/**
 *  An input filter for gzip compression similar to [java.util.zip.GZIPInputStream].
 * 
 *  This filter supports concatenated gzip streams and will keep trying to parse new gzip headers
 *  after a stream has been fully read. If a gzip stream is followed by non-gzip data, this
 *  [GzipBufferedInput] ends successfully instead of reporting an error. This condition can be
 *  checked with [#hasTrailingGarbage()]. The behavior is consistent with the `gunzip` command
 *  line utility. How much of the trailing garbage has been consumed from the parent
 *  [BufferedInput] is undefined.
 * 
 *  In the case of concatenated streams, the metadata methods [#getName()], [#getComment()],
 *  [#getMtimeRaw()] and [#getMtime()] return the metadata of the current gzip stream. In
 *  particular, they return the metadata of the first stream directly after the [GzipBufferedInput]
 *  is created, and the last stream after the [GzipBufferedInput] has been fully read.
 */
public class GzipBufferedInput extends FilteringBufferedInput {
    private final CRC32 crc = new CRC32();
    private String name;
    private String comment;
    private int mtime;
    private Inflater inf = new Inflater(true);
    private int state = STATE_INFLATE;
    private static final int STATE_INFLATE = 0;
    private static final int STATE_TRAILER = 1;
    private static final int STATE_FINISHED = 2;
    private boolean pendingOutput;
    private boolean trailingGarbage;

    public GzipBufferedInput(BufferedInput parent, int blockSize) throws IOException {
        super(parent, blockSize);
        parseHeader(parentView);
    }

    public GzipBufferedInput(BufferedInput parent) throws IOException {
        this(parent, BufferedInput.DEFAULT_BUFFER_SIZE);
    }

    /**
     *  Return the name from the current gzip stream, or null if it has none.
     */
    public String getName() {
        return name;
    }

    /**
     *  Return the comment from the current gzip stream, or null if it has none.
     */
    public String getComment() {
        return comment;
    }

    /**
     *  Return the modification time from the current gzip stream in seconds since
     *  epoch, or 0 if unknown.
     */
    public int getMtimeRaw() {
        return mtime;
    }

    /**
     *  Return the modification time from the current gzip stream, or `null` if unknown.
     */
    public Instant getMtime() {
        return mtime == 0 ? null : Instant.ofEpochSecond(mtime);
    }

    /**
     *  Returns true if there was non-gzip data after one or more successfully read gzip
     *  streams. This flag is not set until the end of this [GzipBufferedInput] has been
     *  reached.
     */
    public boolean hasTrailingGarbage() {
        return trailingGarbage;
    }

    private void parseHeader(BufferedInput bin) throws IOException {
        crc.reset();
        TracingBufferedInput cin = TracingBufferedInput.checked(bin, crc);
        if (cin.uint16l() != 0x8b1f) throw new ZipException("Not in GZIP format");
        if (cin.uint8() != 8) throw new ZipException("Unsupported compression method");
        int flg = cin.uint8();
        int mtime = cin.int32l();
        cin.skip(2); // XFL, OS
        String name, comment;
        if ((flg & 4) != 0) cin.skip(cin.uint16l()); // XLEN, FEXTRA
        if ((flg & 8) != 0) name = cin.zstring(StandardCharsets.ISO_8859_1); // FNAME
        else name = null;
        if ((flg & 16) != 0) comment = cin.zstring(StandardCharsets.ISO_8859_1); // FCOMMENT
        else comment = null;
        if ((flg & 2) != 0) { // FHCRC
            cin.updateTrace();
            int v = (int) crc.getValue() & 0xffff;
            if (cin.uint16l() != v) throw new ZipException("Corrupt GZIP header");
        }
        cin.close(false);
        crc.reset();
        // Update atomically once we successfully read the whole header
        this.mtime = mtime;
        this.name = name;
        this.comment = comment;
    }

    private void parseTrailer(BufferedInput in) throws IOException {
        long c = in.uint32l();
        if (c != crc.getValue()) throw new ZipException("Corrupt GZIP trailer");
        long l = in.uint32l();
        if (l != (inf.getBytesWritten() & 0xffffffffL)) throw new ZipException("Corrupt GZIP trailer");
    }

    @Override
    protected void cleanUp() {
        inf.end();
        inf = null;
    }

    protected void filterBlock(BufferedInput from, WritableBuffer<?> to) throws IOException {
        try {
            int written = 0;
            while (state != STATE_FINISHED) {
                if (state == STATE_INFLATE) {
                    if (inf.finished() || inf.needsDictionary()) {
                        state = STATE_TRAILER;
                        parentView.setPosition(parentView.getPosition() - inf.getRemaining());
                        continue;
                    }
                    if (written != 0) return;
                    if (inf.needsInput() && !pendingOutput) {
                        from.requestAvailable(1);
                        if (from.available() <= 0) throw new EOFException("Unexpected end of ZLIB input stream");
                        if (from.getBuffer() != null)
                            inf.setInput(from.getBuffer(), from.getPosition(), from.getLimit() - from.getPosition());
                        else {
                            ByteBuffer buffer1 = from.getByteBuffer().duplicate();
                            buffer1.position(from.getPosition());
                            buffer1.limit(from.getLimit());
                            InflaterUtil.setInput(inf, buffer1);
                        }
                        from.setPosition(from.getLimit());
                    }
                    int len = to.getLimit() - to.getPosition();
                    written = inf.inflate(to.getBuffer(), to.getPosition(), len);
                    // We return in the next loop if written != 0 so we can verify the trailer first if we're
                    // at the end of the stream
                    crc.update(to.getBuffer(), to.getPosition(), written);
                    to.setPosition(to.getPosition() + written);
                    pendingOutput = written == len;
                } else { // STATE_TRAILER
                    parseTrailer(from);
                    // Check for concatenated stream
                    from.requestAvailable(18); // minimum length of gzip stream
                    int av = from.available();
                    if (av > 0) trailingGarbage = true;
                    if (av >= 18) {
                        try {
                            parseHeader(from);
                            state = STATE_INFLATE;
                            inf.reset();
                            pendingOutput = false;
                            trailingGarbage = false;
                            continue;
                        } catch (IOException ignored) {
                        }
                    }
                    state = STATE_FINISHED;
                }
            }
        } catch (DataFormatException ex) {
            String s = ex.getMessage();
            throw new ZipException(s != null ? s : "Invalid ZLIB data format");
        }
    }
}
