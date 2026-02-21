package com.toolbox.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public interface Factory {

    /**
     * Write data to an [OutputStream].
     *
     * @param out        Stream to write to.
     * @param bufferSize Initial buffer size. The buffer is expanded later if necessary.
     */
    Output writerOf(OutputStream out, int bufferSize);

    /**
     * Write data to an [OutputStream] using the default initial buffer size.
     *
     * @param out Stream to write to.
     * @see #writerOf(OutputStream, int)
     */
    Output writerOf(OutputStream out);

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
    Output writerGrowing(int rootBufferSize, int bufferSize);

    /**
     * Write data to an internal byte array buffer that can be accessed directly or copied after closing<br/>
     * the BufferedOutput (similar to [ByteArrayOutputStream]) using the default initial buffer size.<br/>
     * The size is limited to 2 GB (the maximum size of a byte array).<br/>
     *
     * @param rootBufferSize Initial size of the root buffer. The buffer is expanded later if necessary.
     *                       This size is automatically expanded to `bufferSize` if the specified size is lower.
     * @see #writerGrowing(int, int)
     */
    Output writerGrowing(int rootBufferSize);

    /**
     * Write data to an internal byte array buffer that can be accessed directly or copied after closing<br/>
     * the BufferedOutput (similar to [ByteArrayOutputStream]) using the default initial buffer size and<br/>
     * default initial root buffer size. The size is limited to 2 GB (the maximum size of a byte array).<br/>
     *
     * @see #writerGrowing(int, int)
     */
    Output writerGrowing();

    /**
     * Write data to an internal buffer that can be read as an [InputStream] or
     * [BufferedInput] after closing the BufferedOutput.
     *
     * @param bufferSize Initial buffer size. The buffer is expanded later if necessary.
     */
    Output writerOfBlocks(int bufferSize);

    /**
     * Write data to an internal buffer that can be read as an [InputStream] or [BufferedInput]
     * after closing the BufferedOutput using the default initial buffer size.
     *
     * @see #writerOfBlocks(int)
     */
    Output writerOfBlocks();

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
    Output writerPipe(int bufferSize);

    /**
     * Write data to a buffered pipe that can be read concurrently from another thread as an
     * [InputStream] or [BufferedInput] using the default initial buffer size.
     *
     * @see #writerPipe(int)
     */
    Output writerPipe();

    /**
     * Write data to a given region of an existing byte array. The BufferedOutput is limited to the initial size.
     *
     * @param bufferSize Initial buffer size for additional temporary buffers that may be<br/>
     *                   created when using [#defer(long)], otherwise unused.
     */
    Output writerOf(byte[] buf, int off, int len, int bufferSize);

    /**
     * Write data to a given byte array. Same as `ofArray(buffer, 0, buffer.length)`.
     *
     * @see #writerOf(byte[], int, int, int)
     */
    Output writerOf(byte[] buffer);

    /**
     * Write data to a given region of an existing byte array.
     *
     * @see #writerOf(byte[], int, int, int)
     */
    Output writerOf(byte[] buffer, int start, int len);

    /**
     * Write data to a file.
     *
     * @param path       File to write.
     * @param bufferSize Initial buffer size. The buffer is expanded later if necessary.
     * @param option     Optional set of OpenOptions. When empty, the default <br/>
     *                   set (`CREATE`, `TRUNCATE_EXISTING`, `WRITE`) is used.
     */
    Output writerOf(Path path, int bufferSize, OpenOption... option) throws IOException;

    Output writerOf(Path path, OpenOption... option) throws IOException;

    Output writerOf(Path path) throws IOException;

    Output writerGzip(Output output) throws IOException;

    Output writerGzipParallel(Output output) throws IOException;


    /**
     * Read data from an [InputStream] using the [#DEFAULT_BUFFER_SIZE] and [ByteOrder#BIG_ENDIAN].
     * Same as `of(in, DEFAULT_BUFFER_SIZE)`.
     *
     * @param in InputStream from which to read data.
     * @see #readerOf(InputStream, int)
     */
    Input readerOf(InputStream in);

    /**
     * Read data from an [InputStream] using the default [ByteOrder#BIG_ENDIAN].
     *
     * @param in         InputStream from which to read data.
     * @param bufferSize Initial size of the buffer in bytes. It is automatically extended if necessary.<br/>
     *                   This also affects the minimum block size to read from the InputStream.<br/>
     *                   BufferedInput tries to read at least half of initialBufferSize at once.<br/>
     */
    Input readerOf(InputStream in, int bufferSize);

    /**
     * Same as `reader(buffer, 0, buffer.length)`.
     *
     * @see #readerOf(byte[], int, int)
     */
    Input readerOf(byte[] buffer);

    /**
     * Read data from part of a byte array using the default [ByteOrder#BIG_ENDIAN].
     *
     * @param buffer Array from which to read data.
     * @param offset Starting point within the array.
     * @param length Length of the data within the array.
     */
    Input readerOf(byte[] buffer, int offset, int length);

    /**
     * Read data from a [ByteBuffer]. The BufferedInput will start at the current position and end at the current
     * limit of the ByteBuffer. The initial byte order is also taken from the ByteBuffer. The ByteBuffer's position
     * may be temporarily modified during initialization so it should not be used concurrently. After this method
     * returns, the ByteBuffer is treated as read-only and never modified (including its position, limit and byte order).
     */
    Input readerOf(ByteBuffer byteBuffer);

    /**
     * Read data from a file which is memory-mapped for efficient reading.
     *
     * @param file File to read.
     */
    Input readerOf(Path file) throws IOException;

    Input gzip(Input input) throws IOException;
}
