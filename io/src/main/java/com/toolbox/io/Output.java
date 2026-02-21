package com.toolbox.io;

import com.toolbox.io.text.output.TextOutput;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.Charset;

public interface Output extends Appendable, Flushable, Closeable {

    Output newLine() throws IOException;

    Output appendNull() throws IOException;

    Output append(boolean b) throws IOException;

    Output append(Enum<?> e) throws IOException;

    Output append(byte b) throws IOException;

    Output append(short s) throws IOException;

    Output append(int i) throws IOException;

    Output append(long l) throws IOException;

    Output append(float f) throws IOException;

    Output append(double d) throws IOException;

    Output append(byte[] buffer) throws IOException;

    Output append(byte[] buffer, int offset, int length) throws IOException;

    Output append(String string) throws IOException;

    Output append(String string, Charset charset) throws IOException;

    Output reserve(long length) throws IOException;

    Output defer(long max) throws IOException;

    Output defer() throws IOException;

    TextOutput text(Charset charset, String lineSeparator);

    TextOutput text(Charset charset);

    TextOutput text();

    default byte[] toByteArray() throws IOException {
        throw new UnsupportedOperationException("This output does not support conversion to byte array");
    }
}