package com.toolbox.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

public interface Input extends Closeable {

    int read() throws IOException;

    int read(byte[] buffer, int offset, int length) throws IOException;

    byte[] read(int length) throws IOException;

    byte readByte() throws IOException;

    short readShort() throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    float readFloat() throws IOException;

    double readDouble() throws IOException;

    String readString(int length, Charset charset) throws IOException;

    String readString(int length) throws IOException;

    LineTokenizer lines() throws IOException;

    long skip(long n) throws IOException;

    int available() throws IOException;

    boolean hasMore() throws IOException;
}
