package com.toolbox.io;

import java.nio.ByteBuffer;

public final class ByteBufferMemoryAccessor implements MemoryAccessor {
    private ByteBuffer buffer;

    public ByteBufferMemoryAccessor(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public byte getByte(long offset) { return buffer.get((int) offset); }
    public int getInt(long offset) { return buffer.getInt((int) offset); }
    public long getLong(long offset) { return buffer.getLong((int) offset); }
    public short getShort(long offset) { return buffer.getShort((int) offset); }
    public float getFloat(long offset) { return buffer.getFloat((int) offset); }
    public double getDouble(long offset) { return buffer.getDouble((int) offset); }

    public void putByte(long offset, byte value) { buffer.put((int) offset, value); }
    public void putShort(long offset, short value) { buffer.putShort((int) offset, value); }
    public void putInt(long offset, int value) { buffer.putInt((int) offset, value); }
    public void putLong(long offset, long value) { buffer.putLong((int) offset, value); }

    public void getBytes(long offset, byte[] dst, int dstOffset, int length) {
        int oldPos = buffer.position();
        buffer.position((int) offset);
        buffer.get(dst, dstOffset, length);
        buffer.position(oldPos);
    }

    public void putBytes(long offset, byte[] src, int srcOffset, int length) {
        int oldPos = buffer.position();
        buffer.position((int) offset);
        buffer.put(src, srcOffset, length);
        buffer.position(oldPos);
    }

    public long capacity() { return buffer.capacity(); }

    public void updateBase(Object base, long offset, long size) {
        this.buffer = (ByteBuffer) base;
    }
}