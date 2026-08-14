package com.toolbox.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.zip.Deflater;

public class BaseFactory implements Factory {

    public static final BaseFactory INSTANCE = new BaseFactory();

    public BufferedOutput writerOf(OutputStream out, int bufferSize) {
//        return BufferedOutput.of(out, bufferSize);
        byte[] buf = new byte[Math.max(bufferSize, BufferedOutput.MIN_BUFFER_SIZE)];
        return new FlushingBufferedOutput(buf, true, 0, 0, buf.length, bufferSize, false, Long.MAX_VALUE, out);
    }

    public BufferedOutput writerOf(OutputStream out) {
        return writerOf(out, BufferedOutput.DEFAULT_BUFFER_SIZE);
    }

    public ArrayBufferedOutput writerGrowing(int rootBufferSize, int bufferSize) {
//        return BufferedOutput.growing(rootBufferSize, bufferSize);
        int ibs = Math.max(bufferSize, BufferedOutput.MIN_BUFFER_SIZE);
        byte[] buf = new byte[Math.max(rootBufferSize, ibs)];
        return new ArrayBufferedOutput(buf, true, 0, 0, buf.length, ibs, false);
    }

    public ArrayBufferedOutput writerGrowing(int rootBufferSize) {
//        return BufferedOutput.growing(rootBufferSize);
        return writerGrowing(rootBufferSize, BufferedOutput.DEFAULT_BUFFER_SIZE);
    }

    public ArrayBufferedOutput writerGrowing() {
//        return BufferedOutput.growing();
        return writerGrowing(BufferedOutput.DEFAULT_BUFFER_SIZE);
    }

    public AccumulatingBufferedOutput writerOfBlocks(int bufferSize) {
//        return BufferedOutput.ofBlocks(bufferSize);
        return new BlockBufferedOutput(true, bufferSize);
    }

    public AccumulatingBufferedOutput writerOfBlocks() {
//        return BufferedOutput.ofBlocks();
        return writerOfBlocks(BufferedOutput.DEFAULT_BUFFER_SIZE);
    }

    public PipeBufferedOutput writerPipe(int bufferSize) {
//        return BufferedOutput.pipe(bufferSize);
        return new PipeBufferedOutput(true, bufferSize, 2);
    }

    public PipeBufferedOutput writerPipe() {
//        return BufferedOutput.pipe();
        return writerPipe(BufferedOutput.DEFAULT_BUFFER_SIZE);
    }

    public Output writerOfArray(byte[] buf, int off, int len, int bufferSize) {
//        return BufferedOutput.ofArray(buf, off, len, bufferSize);
        IndexCheckUtil.checkFromIndexSize(off, len, buf.length);
        return new ArrayBufferedOutput(buf, true, off, off, len + off, bufferSize, true);
    }

    public Output writerOfArray(byte[] buffer) {
//        return BufferedOutput.ofArray(buffer);
        return writerOfArray(buffer, 0, buffer.length, BufferedOutput.DEFAULT_BUFFER_SIZE);
    }

    public Output writerOfArray(byte[] buffer, int start, int len) {
//        return BufferedOutput.ofArray(buffer, start, len);
        return writerOfArray(buffer, start, len, BufferedOutput.DEFAULT_BUFFER_SIZE);
    }

    public Output writerOfFile(Path path, int bufferSize, OpenOption... option) throws IOException {
//        return BufferedOutput.ofFile(path, bufferSize, option);
        return writerOf(Files.newOutputStream(path, option), bufferSize);
    }

    public Output writerOfFile(Path path, OpenOption... option) throws IOException {
//        return BufferedOutput.ofFile(path, option);
        return writerOfFile(path, BufferedOutput.DEFAULT_BUFFER_SIZE, option);
    }

    public Output writerOfFile(Path path) throws IOException {
//        return BufferedOutput.ofFile(path);
        return writerOfFile(path, BufferedOutput.DEFAULT_BUFFER_SIZE);
    }

    public GzipBufferedOutput writerGzipSync(BufferedOutput parent) throws IOException {
//        return GzipBufferedOutput.sync(cast(output));
        return writerGzipSync(parent, Deflater.DEFAULT_COMPRESSION);
    }

    public GzipBufferedOutput writerGzipSync(BufferedOutput parent, int compression) throws IOException {
//        return GzipBufferedOutput.sync(cast(output));
        return new SequentialGzipBufferedOutput(cast(parent), compression);
    }
    public GzipBufferedOutput writerGzipParallel(BufferedOutput parent) throws IOException {
        return writerGzipParallel(parent, -1, 65536, 65536, true, null, Deflater.DEFAULT_COMPRESSION);
    }
    public GzipBufferedOutput writerGzipParallel(BufferedOutput parent, int depth, int minPartitionSize,
                                                 int maxPartitionSize, boolean batchSubmit, Executor pool, int compression)
            throws IOException{
        return new ParallelGzipBufferedOutput(parent, depth, minPartitionSize, maxPartitionSize, batchSubmit, pool, compression);
    }


    public BufferedInput readerOf(InputStream in) {
        return readerOf(in, BufferedInput.DEFAULT_BUFFER_SIZE);
    }

    public BufferedInput readerOf(InputStream in, int bufferSize) {
//        return BufferedInput.of(in, bufferSize);
        byte[] buf = new byte[Math.max(bufferSize, BufferedInput.MIN_BUFFER_SIZE)];
        return new StreamingHeapBufferedInput(buf, buf.length, buf.length, Long.MAX_VALUE, in, buf.length / 2, null, true);
    }

    public  BufferedInput readerOfArray(byte[] buf) {
        return readerOfArray(buf, 0, buf.length);
    }

    public BufferedInput readerOfArray(byte[] buffer, int offset, int length) {
        IndexCheckUtil.checkFromIndexSize(offset, length, buffer.length);
        return new StreamingHeapBufferedInput(buffer, offset, offset + length, Long.MAX_VALUE, null, 0, null, true);
    }

    public BufferedInput readerOfByteBuffer(ByteBuffer byteBuffer) {
        return BufferedInput.ofByteBuffer(byteBuffer);
    }

    public BufferedInput readerOfMappedFile(Path file) throws IOException {
        return BufferedInput.ofMappedFile(file);
    }

    public GzipBufferedInput gzip(Input input) throws IOException {
        if (input instanceof BufferedInput) {
            return new GzipBufferedInput((BufferedInput) input);
        }
        throw new IllegalArgumentException("Input must be instance of BufferedInput");
    }

    protected BufferedOutput cast(Output out) {
        if (out instanceof BufferedOutput) return (BufferedOutput) out;
        throw new IllegalArgumentException("Output must be instance of BufferedOutput");
    }
}