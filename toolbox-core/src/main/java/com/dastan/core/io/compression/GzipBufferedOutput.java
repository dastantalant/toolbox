package com.dastan.core.io.compression;

import com.dastan.core.io.spi.AsyncFilteringBufferedOutput;
import com.dastan.core.io.buffer.BufferedOutput;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

/// An output filter for gzip compression similar to [java.util.zip.GZIPOutputStream], but with
/// support for asynchronous and parallel compression.
///
/// Asynchronous processing runs the compression on another thread. This has a very small overhead
/// and is usually faster unless the main thread can produce output at the maximum speed.
///
/// Parallel processing uses multiple threads to compress blocks (with a default size of 64k,
/// independent of the BufferedOutput block size) in parallel which is much faster. Output sizes
/// can be a bit larger due to using a new dictionary for each block.
public abstract class GzipBufferedOutput extends AsyncFilteringBufferedOutput<Deflater> {
    /// Create a new synchronous GzipBufferedOutput with default parameters.
    ///
    /// This is the same as `sync(parent, Deflater.DEFAULT_COMPRESSION)`.
    public static GzipBufferedOutput sync(BufferedOutput parent) throws IOException {
        return sync(parent, Deflater.DEFAULT_COMPRESSION);
    }

    /// Create a new synchronous GzipBufferedOutput.
    ///
    /// @param compression The compression level, one of the constants in {@link Deflater}.
    public static GzipBufferedOutput sync(BufferedOutput parent, int compression) throws IOException {
        return new SequentialGzipBufferedOutput(parent, compression);
    }

    /// Create a new asynchronous GzipBufferedOutput.
    ///
    /// See [AsyncFilteringBufferedOutput#AsyncFilteringBufferedOutput(BufferedOutput , boolean, int, boolean, int, int, boolean, java.util.concurrent.Executor)]
    /// for an explanation of most parameters.
    ///
    /// @param compression The compression level, one of the constants in {@link Deflater}.
    public static GzipBufferedOutput async(BufferedOutput parent, int depth, int minPartitionSize,
                                           int maxPartitionSize, boolean batchSubmit, Executor pool, int compression) throws IOException {
        return new SequentialGzipBufferedOutput(parent, depth, minPartitionSize, maxPartitionSize, batchSubmit, pool, compression);
    }

    /// Create a new asynchronous GzipBufferedOutput with default parameters.
    ///
    /// This is the same as `async(parent, 3, 65536, 0, true, null, Deflater.DEFAULT_COMPRESSION)`.
    public static GzipBufferedOutput async(BufferedOutput parent) throws IOException {
        return async(parent, 3, 65536, 0, true, null, Deflater.DEFAULT_COMPRESSION);
    }

    /// Create a new parallel GzipBufferedOutput.
    ///
    /// See [AsyncFilteringBufferedOutput#AsyncFilteringBufferedOutput(BufferedOutput , boolean, int, boolean, int, int, boolean, java.util.concurrent.Executor)]
    /// for an explanation of most parameters.
    ///
    /// @param compression The compression level, one of the constants in {@link Deflater}.
    public static GzipBufferedOutput parallel(BufferedOutput parent, int depth, int minPartitionSize,
                                              int maxPartitionSize, boolean batchSubmit, Executor pool, int compression) throws IOException {
        return new ParallelGzipBufferedOutput(parent, depth, minPartitionSize, maxPartitionSize, batchSubmit, pool, compression);
    }

    /// Create a new parallel GzipBufferedOutput with default parameters.
    /// This is the same as `parallel(parent, -1, 65536, 65536, true, null, Deflater.DEFAULT_COMPRESSION)`.
    public static GzipBufferedOutput parallel(BufferedOutput parent) throws IOException {
        return parallel(parent, -1, 65536, 65536, true, null, Deflater.DEFAULT_COMPRESSION);
    }


    protected final CRC32 crc = new CRC32();
    private final int compression;

    protected GzipBufferedOutput(BufferedOutput parent, boolean sequential, int depth,
                                 int minPartitionSize, int maxPartitionSize, boolean batchSubmit, Executor pool,
                                 int compression) throws IOException {
        super(parent, sequential, depth, true, minPartitionSize, maxPartitionSize, batchSubmit, pool);
        this.compression = compression;
        writeHeader(parent);
    }

    protected GzipBufferedOutput(BufferedOutput parent, int compression) throws IOException {
        super(parent, true);
        this.compression = compression;
        writeHeader(parent);
    }

    protected Deflater makeDeflater() {
        return new Deflater(compression, true);
    }

    private void writeHeader(BufferedOutput b) throws IOException {
        b.int64l(0x88b1f).int16l((short) 0xff00);
    }

    protected void writeTrailer(BufferedOutput b, long crc, int len) throws IOException {
        b.int32l((int) crc).int32l(len);
    }
}




