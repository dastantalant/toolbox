package com.toolbox.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.zip.Deflater;

public interface Factory {

    /**
     * Write data to an [OutputStream].
     *
     * @param out        Stream to write to.
     * @param bufferSize Initial buffer size. The buffer is expanded later if necessary.
     */
    BufferedOutput writerOf(OutputStream out, int bufferSize);

    /**
     * Write data to an [OutputStream] using the default initial buffer size.
     *
     * @param out Stream to write to.
     * @see #writerOf(OutputStream, int)
     */
    BufferedOutput writerOf(OutputStream out);

    /**
     * Write data to an internal byte array buffer that can be accessed directly or copied after closing<br/>
     * the BufferedOutput (similar to [ByteArrayOutputStream]).<br/>
     * The size is limited to 2 GB (the maximum size of a byte array).<br/>
     *
     * @param rootBufferSize Initial size of the root buffer. The buffer is expanded later if necessary.
     *                       This size is automatically expanded to `bufferSize` if the specified size is lower.
     * @param bufferSize     Initial buffer size for additional buffers that may need to be created.
     *                       This size is automatically expanded to [#MIN_BUFFER_SIZE] if the specified size is lower.
     */
    ArrayBufferedOutput writerGrowing(int rootBufferSize, int bufferSize);

    /**
     * Write data to an internal byte array buffer that can be accessed directly or copied after closing<br/>
     * the BufferedOutput (similar to [ByteArrayOutputStream]) using the default initial buffer size.<br/>
     * The size is limited to 2 GB (the maximum size of a byte array).<br/>
     *
     * @param rootBufferSize Initial size of the root buffer. The buffer is expanded later if necessary.
     *                       This size is automatically expanded to `bufferSize` if the specified size is lower.
     * @see #writerGrowing(int, int)
     */
    ArrayBufferedOutput writerGrowing(int rootBufferSize);

    /**
     * Write data to an internal byte array buffer that can be accessed directly or copied after closing<br/>
     * the BufferedOutput (similar to [ByteArrayOutputStream]) using the default initial buffer size and<br/>
     * default initial root buffer size. The size is limited to 2 GB (the maximum size of a byte array).<br/>
     *
     * @see #writerGrowing(int, int)
     */
    ArrayBufferedOutput writerGrowing();

    /**
     * Write data to an internal buffer that can be read as an [InputStream] or
     * [BufferedInput] after closing the BufferedOutput.
     *
     * @param bufferSize Initial buffer size. The buffer is expanded later if necessary.
     */
    AccumulatingBufferedOutput writerOfBlocks(int bufferSize);

    /**
     * Write data to an internal buffer that can be read as an [InputStream] or [BufferedInput]
     * after closing the BufferedOutput using the default initial buffer size.
     *
     * @see #writerOfBlocks(int)
     */
    AccumulatingBufferedOutput writerOfBlocks();

    /**
     * Write data to a buffered pipe that can be read concurrently from another thread as an [InputStream]
     * or [BufferedInput]. Blocks of data are buffered and then transferred to the reader.
     * Note that this BufferedOutput and the opposing BufferedInput or InputStream are not thread-safe,
     * just like any other BufferedOutput/BufferedInput. Only their communication is.
     * This allows you to use one thread for writing and one thread for reading. Doing both from
     * the same thread is not recommended as it may deadlock when the internal buffer runs full or  empty.
     *
     * @param bufferSize Initial buffer size. The buffer is expanded later if necessary.
     */
    PipeBufferedOutput writerPipe(int bufferSize);

    /**
     * Write data to a buffered pipe that can be read concurrently from another thread as an
     * [InputStream] or [BufferedInput] using the default initial buffer size.
     *
     * @see #writerPipe(int)
     */
    PipeBufferedOutput writerPipe();

    /**
     * Write data to a given region of an existing byte array. The BufferedOutput is limited to the initial size.
     *
     * @param bufferSize Initial buffer size for additional temporary buffers that may be<br/>
     *                   created when using [#defer(long)], otherwise unused.
     */
    Output writerOfArray(byte[] buf, int off, int len, int bufferSize);

    /**
     * Write data to a given byte array. Same as `ofArray(buffer, 0, buffer.length)`.
     *
     * @see #writerOfArray(byte[], int, int, int)
     */
    Output writerOfArray(byte[] buffer);

    /**
     * Write data to a given region of an existing byte array.
     *
     * @see #writerOfArray(byte[], int, int, int)
     */
    Output writerOfArray(byte[] buffer, int start, int len);

    /**
     * Write data to a file.
     *
     * @param path       File to write.
     * @param bufferSize Initial buffer size. The buffer is expanded later if necessary.
     * @param option     Optional set of OpenOptions. When empty, the default <br/>
     *                   set (`CREATE`, `TRUNCATE_EXISTING`, `WRITE`) is used.
     */
    Output writerOfFile(Path path, int bufferSize, OpenOption... option) throws IOException;

    Output writerOfFile(Path path, OpenOption... option) throws IOException;

    Output writerOfFile(Path path) throws IOException;

    /**
     * Create a new synchronous GzipBufferedOutput with default parameters.
     * <p>
     * This is the same as `writerGzipSync(parent, Deflater.DEFAULT_COMPRESSION)`.
     */
    GzipBufferedOutput writerGzipSync(BufferedOutput parent) throws IOException;

    /**
     * Create a new synchronous GzipBufferedOutput.
     *
     * @param compression The compression level, one of the constants in {@link Deflater}.
     */
    GzipBufferedOutput writerGzipSync(BufferedOutput parent, int compression) throws IOException;

    /**
     * Create a new parallel GzipBufferedOutput with default parameters.
     * This is the same as `parallel(parent, -1, 65536, 65536, true, null, Deflater.DEFAULT_COMPRESSION)`.
     */
    GzipBufferedOutput writerGzipParallel(BufferedOutput parent) throws IOException;

    /**
     * Create a new parallel GzipBufferedOutput.
     * <p>
     * See [AsyncFilteringBufferedOutput#AsyncFilteringBufferedOutput(BufferedOutput , boolean, int, boolean, int, int, boolean, Executor)]
     * for an explanation of most parameters.
     *
     * @param compression The compression level, one of the constants in {@link Deflater}.
     */
    GzipBufferedOutput writerGzipParallel(BufferedOutput parent, int depth, int minPartitionSize, int maxPartitionSize, boolean batchSubmit, Executor pool, int compression) throws IOException;

    /**
     * Read data from an [InputStream] using the [#DEFAULT_BUFFER_SIZE] and [ByteOrder#BIG_ENDIAN].
     * Same as `of(in, DEFAULT_BUFFER_SIZE)`.
     *
     * @param in InputStream from which to read data.
     * @see #readerOf(InputStream, int)
     */
    BufferedInput readerOf(InputStream in);

    /**
     * Read data from an [InputStream] using the default [ByteOrder#BIG_ENDIAN].
     *
     * @param in         InputStream from which to read data.
     * @param bufferSize Initial size of the buffer in bytes. It is automatically extended if necessary.<br/>
     *                   This also affects the minimum block size to read from the InputStream.<br/>
     *                   BufferedInput tries to read at least half of initialBufferSize at once.<br/>
     */
    BufferedInput readerOf(InputStream in, int bufferSize);
    /**
     * Same as `ofArray(buf, 0, buf.length)`.
     *
     * @see #readerOfArray(byte[], int, int)
     */
      BufferedInput readerOfArray(byte[] buf);


    /**
     * Read data from part of a byte array using the default [ByteOrder#BIG_ENDIAN].
     *
     * @param buffer Array from which to read data.
     * @param offset Starting point within the array.
     * @param length Length of the data within the array.
     */
    BufferedInput readerOfArray(byte[] buffer, int offset, int length);

    /**
     * Read data from a [ByteBuffer]. The BufferedInput will start at the current position and end at the current
     * limit of the ByteBuffer. The initial byte order is also taken from the ByteBuffer. The ByteBuffer's position
     * may be temporarily modified during initialization so it should not be used concurrently. After this method
     * returns, the ByteBuffer is treated as read-only and never modified (including its position, limit and byte order).
     */
    BufferedInput readerOfByteBuffer(ByteBuffer byteBuffer);

    /**
     * Read data from a file which is memory-mapped for efficient reading.
     *
     * @param file File to read.
     */
    BufferedInput readerOfMappedFile(Path file) throws IOException;

    GzipBufferedInput gzip(Input input) throws IOException;
}
