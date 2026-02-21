package com.toolbox.io;

import com.toolbox.io.buffer.BufferedInput;
import com.toolbox.io.buffer.BufferedOutput;
import com.toolbox.io.compression.GzipBufferedInput;
import com.toolbox.io.compression.GzipBufferedOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class Factory implements IFactory {

    public static final Factory INSTANCE = new Factory();

    public Output writerOf(OutputStream out, int bufferSize) {
        return BufferedOutput.of(out, bufferSize);
    }

    public Output writerOf(OutputStream out) {
        return BufferedOutput.of(out);
    }

    public Output writerGrowing(int rootBufferSize, int bufferSize) {
        return BufferedOutput.growing(rootBufferSize, bufferSize);
    }

    public Output writerGrowing(int rootBufferSize) {
        return BufferedOutput.growing(rootBufferSize);
    }

    public Output writerGrowing() {
        return BufferedOutput.growing();
    }

    public Output writerOfBlocks(int bufferSize) {
        return BufferedOutput.ofBlocks(bufferSize);
    }

    public Output writerOfBlocks() {
        return BufferedOutput.ofBlocks();
    }

    public Output writerPipe(int bufferSize) {
        return BufferedOutput.pipe(bufferSize);
    }

    public Output writerPipe() {
        return BufferedOutput.pipe();
    }

    public Output writerOf(byte[] buf, int off, int len, int bufferSize) {
        return BufferedOutput.ofArray(buf, off, len, bufferSize);
    }

    public Output writerOf(byte[] buffer) {
        return BufferedOutput.ofArray(buffer);
    }

    public Output writerOf(byte[] buffer, int start, int len) {
        return BufferedOutput.ofArray(buffer, start, len);
    }

    public Output writerOf(Path path, int bufferSize, OpenOption... option) throws IOException {
        return BufferedOutput.ofFile(path, bufferSize, option);
    }

    public Output writerOf(Path path, OpenOption... option) throws IOException {
        return BufferedOutput.ofFile(path, option);
    }

    public Output writerOf(Path path) throws IOException {
        return BufferedOutput.ofFile(path);
    }

    public Output writerGzip(Output output) throws IOException {
        return GzipBufferedOutput.sync(cast(output));
    }

    public Output writerGzipParallel(Output output) throws IOException {
        return GzipBufferedOutput.parallel(cast(output));
    }

    public Input readerOf(InputStream in) {
        return BufferedInput.of(in);
    }

    public Input readerOf(InputStream in, int bufferSize) {
        return BufferedInput.of(in, bufferSize);
    }

    public Input readerOf(byte[] buffer) {
        return BufferedInput.ofArray(buffer, 0, buffer.length);
    }

    public Input readerOf(byte[] buffer, int offset, int length) {
        return BufferedInput.ofArray(buffer, offset, length);
    }


    public Input readerOf(ByteBuffer byteBuffer) {
        return BufferedInput.ofByteBuffer(byteBuffer);
    }

    public Input readerOf(Path file) throws IOException {
        return BufferedInput.ofMappedFile(file);
    }

    public Input gzip(Input input) throws IOException {
        if (input instanceof BufferedInput) {
            return new GzipBufferedInput((BufferedInput) input);
        }
        throw new IllegalArgumentException("Input must be instance of perfio BufferedInput");
    }

    protected BufferedOutput cast(Output out) {
        if (out instanceof BufferedOutput) return (BufferedOutput) out;
        throw new IllegalArgumentException("Output must be instance of BufferedOutput");
    }
}