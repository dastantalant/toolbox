package com.toolbox.core.io.buffer;

import com.toolbox.core.io.buffer.internal.ArrayBufferedOutput;
import com.toolbox.core.io.buffer.internal.BlockBufferedOutput;
import com.toolbox.core.io.buffer.internal.FlushingBufferedOutput;
import com.toolbox.core.io.buffer.internal.PipeBufferedOutput;
import com.toolbox.core.io.internal.BufferUtil;
import com.toolbox.core.io.text.TextOutput;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;


/// BufferedOutput provides buffered streaming writes to an OutputStream or similar data sink.
///
/// The API is not thread-safe. Access from multiple threads must be synchronized externally.
/// The number of bytes written is tracked as a 64-bit signed long value [#totalBytesWritten()].
/// The behaviour after writing more than [Long#MAX_VALUE] (8 exabytes) is undefined.
///
/// A freshly created BufferedOutput always uses [ByteOrder#BIG_ENDIAN]. This can be changed with
/// [#order(ByteOrder)]. Unless specified explicitly, the initial buffer size is
/// [#DEFAULT_BUFFER_SIZE].
public abstract class BufferedOutput extends WritableBuffer<BufferedOutput> implements Closeable, Flushable {

    /// Write data to an [OutputStream].
    ///
    /// @param out        Stream to write to.
    /// @param bufferSize Initial buffer size. The buffer is expanded later if necessary.
    public static BufferedOutput of(OutputStream out, int bufferSize) {
        var buf = new byte[Math.max(bufferSize, MIN_BUFFER_SIZE)];
        return new FlushingBufferedOutput(buf, true, 0, 0, buf.length, bufferSize, false, Long.MAX_VALUE, out);
    }

    /// Write data to an [OutputStream] using the default initial buffer size.
    ///
    /// @param out Stream to write to.
    /// @see #of(OutputStream, int)
    public static BufferedOutput of(OutputStream out) {
        return of(out, DEFAULT_BUFFER_SIZE);
    }

    /// Write data to an internal byte array buffer that can be accessed directly or copied after
    /// closing the BufferedOutput (similar to [ByteArrayOutputStream]). The size is limited to 2 GB
    /// (the maximum size of a byte array).
    ///
    /// @param rootBufferSize Initial size of the root buffer. The buffer is expanded later if
    ///                                                                                                                 necessary. This size is automatically expanded to `bufferSize` if the specified size
    ///                                                                                                                 is lower.
    /// @param bufferSize     Initial buffer size for additional buffers that may need to be
    ///                                                                                                                 created. This size is automatically expanded to [#MIN_BUFFER_SIZE] if the specified size is
    ///                                                                                                                 lower.
    public static ArrayBufferedOutput growing(int rootBufferSize, int bufferSize) {
        var ibs = Math.max(bufferSize, MIN_BUFFER_SIZE);
        var buf = new byte[Math.max(rootBufferSize, ibs)];
        return new ArrayBufferedOutput(buf, true, 0, 0, buf.length, ibs, false);
    }

    /// Write data to an internal byte array buffer that can be accessed directly or copied after
    /// closing the BufferedOutput (similar to [ByteArrayOutputStream]) using the default initial
    /// buffer size. The size is limited to 2 GB (the maximum size of a byte array).
    ///
    /// @param rootBufferSize Initial size of the root buffer. The buffer is expanded later if
    ///                                                                                                                 necessary. This size is automatically expanded to `bufferSize` if the specified size
    ///                                                                                                                 is lower.
    /// @see #growing(int, int)
    public static ArrayBufferedOutput growing(int rootBufferSize) {
        return growing(rootBufferSize, DEFAULT_BUFFER_SIZE);
    }

    /// Write data to an internal byte array buffer that can be accessed directly or copied after
    /// closing the BufferedOutput (similar to [ByteArrayOutputStream]) using the default initial
    /// buffer size and default initial root buffer size. The size is limited to 2 GB (the maximum
    /// size of a byte array).
    ///
    /// @see #growing(int, int)
    public static ArrayBufferedOutput growing() {
        return growing(DEFAULT_BUFFER_SIZE);
    }

    /// Write data to an internal buffer that can be read as an [InputStream] or [BufferedInput]
    /// after closing the BufferedOutput.
    ///
    /// @param bufferSize Initial buffer size. The buffer is expanded later if necessary.
    public static AccumulatingBufferedOutput ofBlocks(int bufferSize) {
        return new BlockBufferedOutput(true, bufferSize);
    }

    /// Write data to an internal buffer that can be read as an [InputStream] or [BufferedInput]
    /// after closing the BufferedOutput using the default initial buffer size.
    ///
    /// @see #ofBlocks(int)
    public static AccumulatingBufferedOutput ofBlocks() {
        return ofBlocks(DEFAULT_BUFFER_SIZE);
    }

    /// Write data to a buffered pipe that can be read concurrently from another thread as an
    /// [InputStream] or [BufferedInput]. Blocks of data are buffered and then transferred to the
    /// reader. Note that this BufferedOutput and the opposing BufferedInput or InputStream are not
    /// thread-safe, just like any other BufferedOutput/BufferedInput. Only their communication is.
    /// This allows you to use one thread for writing and one thread for reading. Doing both from
    /// the same thread is not recommended as it may deadlock when the internal buffer runs full or
    /// empty.
    ///
    /// @param bufferSize Initial buffer size. The buffer is expanded later if necessary.
    public static PipeBufferedOutput pipe(int bufferSize) {
        return new PipeBufferedOutput(true, bufferSize, 2);
    }

    /// Write data to a buffered pipe that can be read concurrently from another thread as an
    /// [InputStream] or [BufferedInput] using the default initial buffer size.
    ///
    /// @see #pipe(int)
    public static PipeBufferedOutput pipe() {
        return pipe(DEFAULT_BUFFER_SIZE);
    }


    /// Write data to a given region of an existing byte array. The BufferedOutput is limited to the
    /// initial size.
    ///
    /// @param bufferSize Initial buffer size for additional temporary buffers that may be
    ///                                                                                                                    created when using [#defer(long)], otherwise unused.
    public static ArrayBufferedOutput ofArray(byte[] buf, int off, int len, int bufferSize) {
        Objects.checkFromIndexSize(off, len, buf.length);
        return new ArrayBufferedOutput(buf, true, off, off, len + off, bufferSize, true);
    }

    /// Write data to a given byte array. Same as `ofArray(buffer, 0, buffer.length)`.
    ///
    /// @see #ofArray(byte[], int, int, int)
    public static ArrayBufferedOutput ofArray(byte[] buffer) {
        return ofArray(buffer, 0, buffer.length, DEFAULT_BUFFER_SIZE);
    }

    /// Write data to a given region of an existing byte array.
    ///
    /// @see #ofArray(byte[], int, int, int)
    public static ArrayBufferedOutput ofArray(byte[] buffer, int start, int len) {
        return ofArray(buffer, start, len, DEFAULT_BUFFER_SIZE);
    }

    /// Write data to a file.
    ///
    /// @param path       File to write.
    /// @param bufferSize Initial buffer size. The buffer is expanded later if necessary.
    /// @param option     Optional set of OpenOptions. When empty, the default set (`CREATE`,
    ///                                                                                                                           `TRUNCATE_EXISTING`, `WRITE`) is used.
    public static BufferedOutput ofFile(Path path, int bufferSize, OpenOption... option) throws IOException {
        var out = Files.newOutputStream(path, option);
        return of(out, bufferSize);
    }

    public static BufferedOutput ofFile(Path path, OpenOption... option) throws IOException {
        return ofFile(path, DEFAULT_BUFFER_SIZE, option);
    }

    public static BufferedOutput ofFile(Path path) throws IOException {
        return ofFile(path, DEFAULT_BUFFER_SIZE);
    }

    public static final int MIN_BUFFER_SIZE = BufferUtil.VECTOR_LENGTH * 2;
    public static final int DEFAULT_BUFFER_SIZE = BufferUtil.DEFAULT_BUFFER_SIZE;
    protected static final byte SHARING_EXCLUSIVE = (byte) 0;
    protected static final byte SHARING_LEFT = (byte) 1;
    protected  static final byte SHARING_RIGHT = (byte) 2;
    protected  static final byte STATE_OPEN = (byte) 0;
    protected static final byte STATE_CLOSED = (byte) 1;

    protected BufferedOutput(byte[] buf, boolean bigEndian, int start, int pos, int lim, boolean fixed, long totalLimit, TopLevelBufferedOutput topLevel, TopLevelBufferedOutput cache, boolean preferSplit) {
        this.buffer = buf;
        this.isBigEnd = bigEndian;
        this.start = start;
        this.position = pos;
        this.limit = lim;
        this.fixed = fixed;
        this.totalLimit = totalLimit;
        this.topLevel = topLevel == null ? (TopLevelBufferedOutput) this : topLevel;
        this.cache = cache == null ? (TopLevelBufferedOutput) this : cache;
        this.preferSplit = preferSplit;
    }

    protected final boolean fixed;
    protected long totalLimit;
    protected boolean preferSplit;
    protected long totalFlushed = 0L;
    protected BufferedOutput next = this;
    protected BufferedOutput prev = this;
    protected BufferedOutput rootBlock = this;
    protected TopLevelBufferedOutput topLevel;
    public final TopLevelBufferedOutput cache;
    protected Object filterState;
    protected byte sharing = SHARING_EXCLUSIVE;
    protected byte state = STATE_OPEN;
    private TextOutput cachedTextOutput;
    protected boolean truncate = true;

    public boolean isFixed() {
        return fixed;
    }

    public long getTotalLimit() {
        return totalLimit;
    }

    public void setTotalLimit(long totalLimit) {
        this.totalLimit = totalLimit;
    }

    public boolean isPreferSplit() {
        return preferSplit;
    }

    public void setPreferSplit(boolean preferSplit) {
        this.preferSplit = preferSplit;
    }

    public TopLevelBufferedOutput getCache() {
        return cache;
    }

    public Object getFilterState() {
        return filterState;
    }

    public void setFilterState(Object filterState) {
        this.filterState = filterState;
    }

    public byte getSharing() {
        return sharing;
    }

    public void setSharing(byte sharing) {
        this.sharing = sharing;
    }

    public TextOutput getCachedTextOutput() {
        return cachedTextOutput;
    }

    public void setCachedTextOutput(TextOutput cachedTextOutput) {
        this.cachedTextOutput = cachedTextOutput;
    }

    public boolean isTruncate() {
        return truncate;
    }

    public void setTruncate(boolean truncate) {
        this.truncate = truncate;
    }


    public long getTotalFlushed() {
        return totalFlushed;
    }

    public void setTotalFlushed(long totalFlushed) {
        this.totalFlushed = totalFlushed;
    }


    public BufferedOutput getNext() {
        return next;
    }

    public void setNext(BufferedOutput next) {
        this.next = next;
    }

    public BufferedOutput getPrev() {
        return prev;
    }

    public void setPrev(BufferedOutput prev) {
        this.prev = prev;
    }

    public BufferedOutput getRootBlock() {
        return rootBlock;
    }

    public void setRootBlock(BufferedOutput rootBlock) {
        this.rootBlock = rootBlock;
    }

    public TopLevelBufferedOutput getTopLevel() {
        return topLevel;
    }

    public void setTopLevel(TopLevelBufferedOutput topLevel) {
        this.topLevel = topLevel;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    /// Change the byte order of this BufferedOutput.
    public BufferedOutput order(ByteOrder order) {
        isBigEnd = order == ByteOrder.BIG_ENDIAN;
        return this;
    }

    /// Return the initial buffer size. No block of this BufferedOutput will be smaller than this
    /// size (but larger blocks can be allocated if they are requested for large writes).
    public int initialBufferSize() {
        return topLevel.getInitialBufferSize();
    }

    void checkState() throws IOException {
        if (state != STATE_OPEN) throw new IOException("BufferedOutput has already been closed");
    }

    public final long totalBytesWritten() {
        return totalFlushed + (position - start);
    }

    protected void flushAndGrow(int count) throws IOException {
        checkState();
        if (fixed) return;
        if (preferSplit && limit - position <= topLevel.getInitialBufferSize() / 2 && count <= topLevel.getInitialBufferSize()) { //TODO try to flush first?
            // switch to a new buffer if this one is sufficiently filled
            newBuffer();
        } else if (prev == topLevel) {
            topLevel.flushBlocks(false);
            if (limit < position + count) {
                if (position == start) growBufferClear(count);
                else growBufferCopy(count);
            }
        } else growBufferCopy(count);
    }

    private void newBuffer() throws IOException {
        var pre = this.prev;
        var b = cache.getExclusiveBlock();
        totalFlushed += (position - start);
        buffer = b.reinit(buffer, isBigEnd, start, position, limit, sharing, 0L, 0L, true, rootBlock, null, topLevel, preferSplit);
        b.state = STATE_CLOSED;
        b.insertBefore(this);
        start = 0;
        position = 0;
        limit = buffer.length;
        sharing = SHARING_EXCLUSIVE;
        if (pre == topLevel) topLevel.flushBlocks(false);
    }

    private IOException underflow(long len, long exp) {
        return new IOException("Can't close BufferedOutput created by reserve(" + exp + "): Only " + len + " bytes written");
    }

    private void checkUnderflow() throws IOException {
        if (fixed) {
            if (position != limit) throw underflow(position - start, limit - start);
        } else {
            if (totalBytesWritten() != totalLimit) throw underflow(totalLimit, totalBytesWritten());
        }
    }

    public final void mergeToRight() {
        var n = next;
        var nlen = n.position - n.start;
        System.arraycopy(n.buffer, n.start, buffer, position, nlen);
        var len = position - start;
        n.totalFlushed -= len;
        var tmpbuf = buffer;
        buffer = n.buffer;
        n.buffer = tmpbuf;
        n.start = start;
        n.limit = tmpbuf.length;
        n.position = position + nlen;
    }

    void unshare() {
        if (sharing == SHARING_RIGHT) {
            sharing = SHARING_EXCLUSIVE;
            if (prev.sharing == SHARING_LEFT) prev.sharing = SHARING_RIGHT;
        }
    }

    private void growBufferClear(int count) {
        var tlim = (int) Math.min(totalLimit - totalBytesWritten(), count);
        var buflen = BufferUtil.growBuffer(buffer.length, tlim, 1);
        if (buflen > buffer.length) buffer = new byte[buflen];
        limit = buffer.length;
        position = 0;
        start = 0;
        unshare();
    }

    private void growBufferCopy(int count) {
        var tlim = (int) Math.min(totalLimit - totalBytesWritten(), position + count);
        var buflen = BufferUtil.growBuffer(buffer.length, tlim, 2);
        //System.out.println("grow "+buf.length+", "+tlim+", "+buflen);
        if (buflen > buffer.length) buffer = Arrays.copyOf(buffer, buflen);
        limit = buffer.length;
        unshare();
    }

    public void insertBefore(BufferedOutput b) {
        prev = b.prev;
        next = b;
        b.prev.next = this;
        b.prev = this;
    }

    public void insertAllBefore(BufferedOutput b) {
        var n = next;
        n.prev = b.prev;
        b.prev.next = n;
        next = b;
        b.prev = this;
    }

    public void unlinkAndReturn() {
        prev.next = next;
        next.prev = prev;
        cache.returnToCache((NestedBufferedOutput) this);
    }

    public void unlinkOnly() {
        prev.next = next;
        next.prev = prev;
    }

    protected void flushUpstream() throws IOException {
    }

    protected void bufferClosed(boolean closeUpstream) throws IOException {
    }

    public final void flush() throws IOException {
        checkState();
        if (rootBlock == topLevel) {
            topLevel.flushBlocks(true);
            topLevel.flushUpstream();
        }
    }

    public final void close() throws IOException {
        close(true);
    }

    /// Close this BufferedOutput and mark it as closed. Calling this method again has
    /// no effect, calling most other methods after closing results in an [IOException].
    ///
    /// If this is a root BufferedOutput based on an [OutputStream] or similar data sink, any
    /// outstanding data is flushed. In case of an [ArrayBufferedOutput], the data becomes
    /// available for reading once it is closed.
    ///
    /// Closing a nested BufferedOutput created with [#reserve(long)] before writing all the
    /// requested data results in an IOException.
    ///
    /// In all cases any nested BufferedOutputs that have not been closed yet are implicitly
    /// closed when closing this BufferedOutput.
    ///
    /// @param closeUpstream If this is a root BufferedOutput based on a data sink that supports
    ///                                                                                                            closing (like an [OutputStream]), this flag determines whether to close the sink as
    ///                                                                                                            well. It has no effect on views (which never close their parent).
    public final void close(boolean closeUpstream) throws IOException {
        if (state != STATE_OPEN) return;
        if (!truncate) checkUnderflow();
        state = STATE_CLOSED;
        limit = position;
        // We always unlink SHARING_LEFT when closing so we don't have to merge blocks later when
        // flushing. They are always created by reserveShort() and they cannot be root blocks.
        if (sharing == SHARING_LEFT) {
            var n = next;
            n.start = start;
            n.totalFlushed -= (position - start);
            var pre = prev;
            var t = topLevel;
            unlinkAndReturn();
            if (pre == t) t.flushBlocks(false);
        } else {
            if (rootBlock == this) {
                for (var b = next; b != this; ) {
                    var bn = b.next;
                    if (b.state == STATE_OPEN) {
                        if (!b.truncate) b.checkUnderflow();
                        if (b.sharing == SHARING_LEFT) {
                            bn.start = b.start;
                            bn.totalFlushed -= (b.position - b.start);
                            b.unlinkAndReturn();
                        } else {
                            b.limit = b.position;
                            b.state = STATE_CLOSED;
                        }
                    }
                    b = bn;
                }
                if (this == topLevel) topLevel.flushBlocks(true);
            } else if (prev == topLevel) topLevel.flushBlocks(false);
            assert state == STATE_CLOSED;
            assert (position == limit);
            bufferClosed(closeUpstream);
        }
    }

    private BufferedOutput reserveShort(int length) throws IOException {
        var p = fwd(length);
        var len = p - start;
        var b = cache.getSharedBlock();
        b.reinit(buffer, isBigEnd, start, p, position, SHARING_LEFT, length, -len, false, rootBlock, null, topLevel, preferSplit);
        b.insertBefore(this);
        totalFlushed += (position - start);
        start = position;
        if (sharing == SHARING_EXCLUSIVE) sharing = SHARING_RIGHT;
        return b;
    }

    private BufferedOutput reserveLong(long max) throws IOException {
        var len = position - start;
        var b = cache.getExclusiveBlock();
        var l = limit;
        if (l - position > max) l = (int) (max + position);
        buffer = b.reinit(buffer, isBigEnd, start, position, l, sharing, max - len, -len, false, rootBlock, null, topLevel, preferSplit);
        b.insertBefore(this);
        totalFlushed = totalFlushed + len + max;
        sharing = SHARING_EXCLUSIVE;
        position = 0;
        limit = buffer.length;
        return b;
    }

    /// Leave a fixed-size gap at the current position that can be filled later after continuing to
    /// write to this BufferedOutput. [#totalBytesWritten()] is immediately increased by the
    /// requested size. Attempting to write more than the requested amount of data to the returned
    /// BufferedOutput or closing it before writing all of the data throws an IOException.
    ///
    /// @param length The number of bytes to skip in this BufferedOutput and return as a new
    ///                                                                                     BufferedOutput.
    /// @return A new BufferedOutput used to fill the gap.
    public final BufferedOutput reserve(long length) throws IOException {
        checkState();
        if (fixed) return reserveShort((int) Math.min(length, available()));
        else if (length < topLevel.getInitialBufferSize())
            return reserveShort((int) length); //TODO always use reserveShort when it fits?
        else return reserveLong(length);
    }

    /// Create a new BufferedOutput `b` that gets appended to `this` when it is closed. If `this` has a limited
    /// size and appending `b` would exceed the limit, an [EOFException] is thrown when attempting to close `b`.
    /// Attempting to write more than the requested maximum length to `b` results in an [IOException].
    ///
    /// @param max The size limit of the returned BufferedOutput
    /// @return A new BufferedOutput
    public final BufferedOutput defer(long max) throws IOException {
        checkState();
        var b = cache.getExclusiveBlock();
        b.reinit(b.buffer, isBigEnd, 0, 0, b.buffer.length, SHARING_EXCLUSIVE, max, 0L, true, b, this, topLevel, true);
        b.next = b;
        b.prev = b;
        return b;
    }

    /// Same as `defer(Long.MAX_VALUE)`
    ///
    /// @see #defer(long)
    public final BufferedOutput defer() throws IOException {
        return defer(Long.MAX_VALUE);
    }

    /// Append a nested root block.
    public void appendNested(BufferedOutput r) throws IOException {
        var n = appendNestedMerge(r);
        if (!n) appendNestedSwap(r);
    }

    private boolean appendNestedMerge(BufferedOutput r) throws IOException {
        var b = r.next;
        while (true) {
            var blen = b.position - b.start;
            if (limit - position < blen) return false;
            System.arraycopy(b.buffer, b.start, buffer, position, blen);
            position += blen;
            var bn = b.next;
            if (b == r) {
                cache.returnToCache((NestedBufferedOutput) r);
                return true;
            }
            b.unlinkAndReturn();
            //r.totalFlushed -= blen;
            b = bn;
        }
    }

    private void appendNestedSwap(BufferedOutput b) throws IOException {
        var tmpbuf = buffer;
        var tmpstart = start;
        var tmppos = position;
        var tmpsharing = sharing;
        var len = tmppos - tmpstart;
        var blen = b.position - b.start;
        if (totalBytesWritten() + b.totalBytesWritten() > totalLimit) throw new EOFException();
        totalFlushed += b.totalFlushed + len;
        //b.totalFlushed += blen - len;
        buffer = b.buffer;
        start = b.start;
        position = b.position;
        limit = buffer.length;
        sharing = b.sharing;
        b.buffer = tmpbuf;
        b.start = tmpstart;
        b.position = tmppos;
        b.limit = tmppos;
        b.sharing = tmpsharing;
        b.rootBlock = rootBlock;
        var bp = b.prev;
        bp.insertAllBefore(this);
        assert b.state != STATE_OPEN;
        if (bp.prev == topLevel) topLevel.flushBlocks(false);
    }

    private int numBlocks() {
        var b = next;
        int i = 1;
        while (b != this) {
            i++;
            b = b.next;
        }
        return i;
    }

    @Override
    public TextOutput text() {
        if (cachedTextOutput == null) {
            cachedTextOutput = TextOutput.of(this, StandardCharsets.UTF_8, System.lineSeparator());
        }
        return cachedTextOutput;
    }

    @Override
    public TextOutput text(Charset charset, String lineSeparator) {
        if (cachedTextOutput != null && StandardCharsets.UTF_8.equals(charset) && System.lineSeparator().equals(lineSeparator)) {
            return cachedTextOutput;
        }
        return TextOutput.of(this, charset, lineSeparator);
    }

    @Override
    public TextOutput text(Charset charset) {
        return text(charset, System.lineSeparator());
    }

    final String showThis() {
        var builder = new StringBuilder();
        if (this == topLevel) builder.append('*');
        if (this == rootBlock) builder.append('+');
        return builder.append(System.identityHashCode(this))
                .append(':').append(showSharing())
                .append(':').append(showState())
                .append(",start=").append(start)
                .append(",position=").append(position)
                .append(",limit=").append(limit)
                .append(",lenght=").append(position - start)
                .append(",buffer.length=").append(buffer.length)
                .append(",preferSplit=").append(preferSplit)
                .toString();
    }

    final String showContents() {
        var b = new StringBuilder("[");
        for (int i = start; i < position; i++) {
            if (i > start) b.append(',');
            b.append(Integer.toHexString(buffer[i] & 0xff));
        }
        return b.append(']').toString();
    }

    final String showList() {
        var n = rootBlock.next;
        var b = new StringBuilder();
        while (true) {
            if (n == this) b.append('<');
            b.append(n.showThis());
            if (n == this) b.append('>');
            if (n == rootBlock) return b.toString();
            b.append(" -> ");
            n = n.next;
        }
    }

    final String showSharing() {
        return switch (sharing) {
            case SHARING_EXCLUSIVE -> "E";
            case SHARING_LEFT -> "L";
            case SHARING_RIGHT -> "R";
            default -> String.valueOf(sharing);
        };
    }

    final String showState() {
        return switch (state) {
            case STATE_OPEN -> "O";
            case STATE_CLOSED -> "C";
            default -> String.valueOf(state);
        };
    }
}
