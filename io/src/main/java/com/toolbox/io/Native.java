package com.toolbox.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;

public interface Native {
    Native INSTANCE = NativeFactory.create();

    int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;
    int DEFAULT_BUFFER_SIZE = 32768;

    byte[] EMPTY_BUFFER = new byte[0];

    void set(byte[] array, int offset, short value);

    void set(byte[] array, int offset, int value);

    short get(byte[] array, int offset);

    int growBuffer(int current, int target, int align);

    ByteBuffer wrap(byte[] array, int offset, int length, ByteOrder order);

    ByteBuffer wrap(byte[] array, ByteOrder order);

    MappedByteBuffer mapReadOnlyFile(Path file) throws IOException;

    boolean vectorEnabled();

    int vectorLength();
}
