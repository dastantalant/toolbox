package com.toolbox.io;

public interface MemoryAccessor {
    byte getByte(long offset);

    short getShort(long offset);

    int getInt(long offset);

    long getLong(long offset);

    float getFloat(long offset);

    double getDouble(long offset);

    void putByte(long offset, byte value);

    void putShort(long offset, short value);

    void putInt(long offset, int value);

    void putLong(long offset, long value);

    void getBytes(long offset, byte[] dst, int dstOffset, int length);

    void putBytes(long offset, byte[] src, int srcOffset, int length);

    long capacity();

    void updateBase(Object base, long offset, long size);
}