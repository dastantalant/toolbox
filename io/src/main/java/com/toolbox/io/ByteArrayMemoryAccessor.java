package com.toolbox.io;

public final class ByteArrayMemoryAccessor implements MemoryAccessor {
    private byte[] array;
    private long size;

    public ByteArrayMemoryAccessor(byte[] array) {
        this.array = array;
        this.size = array.length;
    }

    // Пример реализации (остальные методы аналогично через Unsafe или array access)
    public byte getByte(long offset) { return array[(int) offset]; }
    public void putByte(long offset, byte value) { array[(int) offset] = value; }

    public int getInt(long offset) {
        int i = (int) offset;
        // Big-endian default implementation
        return ((array[i] & 0xFF) << 24) |
                ((array[i + 1] & 0xFF) << 16) |
                ((array[i + 2] & 0xFF) << 8) |
                (array[i + 3] & 0xFF);
    }

    public void putInt(long offset, int value) {
        int i = (int) offset;
        array[i]     = (byte) (value >> 24);
        array[i + 1] = (byte) (value >> 16);
        array[i + 2] = (byte) (value >> 8);
        array[i + 3] = (byte) value;
    }

    public long getLong(long offset) { /* ... bitwise ops ... */ return 0; }
    public short getShort(long offset) { return 0; }
    public float getFloat(long offset) { return Float.intBitsToFloat(getInt(offset)); }
    public double getDouble(long offset) { return Double.longBitsToDouble(getLong(offset)); }
    public void putShort(long offset, short value) {}
    public void putLong(long offset, long value) {}

    public void getBytes(long offset, byte[] dst, int dstOffset, int length) {
        System.arraycopy(array, (int) offset, dst, dstOffset, length);
    }

    public void putBytes(long offset, byte[] src, int srcOffset, int length) {
        System.arraycopy(src, srcOffset, array, (int) offset, length);
    }

    public long capacity() { return size; }

    public void updateBase(Object base, long offset, long size) {
        this.array = (byte[]) base;
        this.size = size;
    }
}