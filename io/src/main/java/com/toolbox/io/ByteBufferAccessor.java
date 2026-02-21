package com.toolbox.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class ByteBufferAccessor implements Accessor {
    ByteBufferAccessor() {
    }

    @Override
    public byte int8(byte[] a, int i) {
        return a[i];
    }

    @Override
    public void int8(byte[] a, int i, byte v) {
        a[i] = v;
    }

    @Override
    public short int16(byte[] a, int i, boolean big) {
        return ByteBuffer.wrap(a, i, 2).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getShort();
    }

    @Override
    public short int16n(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 2).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getShort();
    }

    @Override
    public short int16b(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 2).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    @Override
    public short int16l(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    @Override
    public void int16(byte[] a, int i, short v, boolean big) {
        ByteBuffer.wrap(a, i, 2).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putShort(v);
    }

    @Override
    public void int16n(byte[] a, int i, short v) {
        ByteBuffer.wrap(a, i, 2).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putShort(v);
    }

    @Override
    public void int16b(byte[] a, int i, short v) {
        ByteBuffer.wrap(a, i, 2).order(ByteOrder.BIG_ENDIAN).putShort(v);
    }

    @Override
    public void int16l(byte[] a, int i, short v) {
        ByteBuffer.wrap(a, i, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(v);
    }

    @Override
    public char uint16(byte[] a, int i, boolean big) {
        return (char) ByteBuffer.wrap(a, i, 2).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getShort();
    }

    @Override
    public char uint16n(byte[] a, int i) {
        return (char) ByteBuffer.wrap(a, i, 2).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getShort();
    }

    @Override
    public char uint16b(byte[] a, int i) {
        return (char) ByteBuffer.wrap(a, i, 2).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    @Override
    public char uint16l(byte[] a, int i) {
        return (char) ByteBuffer.wrap(a, i, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    @Override
    public void uint16(byte[] a, int i, char v, boolean big) {
        ByteBuffer.wrap(a, i, 2).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putShort((short) v);
    }

    @Override
    public void uint16n(byte[] a, int i, char v) {
        ByteBuffer.wrap(a, i, 2).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putShort((short) v);
    }

    @Override
    public void uint16b(byte[] a, int i, char v) {
        ByteBuffer.wrap(a, i, 2).order(ByteOrder.BIG_ENDIAN).putShort((short) v);
    }

    @Override
    public void uint16l(byte[] a, int i, char v) {
        ByteBuffer.wrap(a, i, 2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) v);
    }

    @Override
    public int int32(byte[] a, int i, boolean big) {
        return ByteBuffer.wrap(a, i, 4).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getInt();
    }

    @Override
    public int int32n(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 4).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getInt();
    }

    @Override
    public int int32b(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 4).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    @Override
    public int int32l(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    @Override
    public void int32(byte[] a, int i, int v, boolean big) {
        ByteBuffer.wrap(a, i, 4).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putInt(v);
    }

    @Override
    public void int32n(byte[] a, int i, int v) {
        ByteBuffer.wrap(a, i, 4).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putInt(v);
    }

    @Override
    public void int32b(byte[] a, int i, int v) {
        ByteBuffer.wrap(a, i, 4).order(ByteOrder.BIG_ENDIAN).putInt(v);
    }

    @Override
    public void int32l(byte[] a, int i, int v) {
        ByteBuffer.wrap(a, i, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(v);
    }

    @Override
    public long int64(byte[] a, int i, boolean big) {
        return ByteBuffer.wrap(a, i, 8).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getLong();
    }

    @Override
    public long int64n(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 8).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getLong();
    }

    @Override
    public long int64b(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 8).order(ByteOrder.BIG_ENDIAN).getLong();
    }

    @Override
    public long int64l(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    @Override
    public void int64(byte[] a, int i, long v, boolean big) {
        ByteBuffer.wrap(a, i, 8).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putLong(v);
    }

    @Override
    public void int64n(byte[] a, int i, long v) {
        ByteBuffer.wrap(a, i, 8).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putLong(v);
    }

    @Override
    public void int64b(byte[] a, int i, long v) {
        ByteBuffer.wrap(a, i, 8).order(ByteOrder.BIG_ENDIAN).putLong(v);
    }

    @Override
    public void int64l(byte[] a, int i, long v) {
        ByteBuffer.wrap(a, i, 8).order(ByteOrder.LITTLE_ENDIAN).putLong(v);
    }

    @Override
    public float float32(byte[] a, int i, boolean big) {
        return ByteBuffer.wrap(a, i, 4).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    @Override
    public float float32n(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 4).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    @Override
    public float float32b(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 4).order(ByteOrder.BIG_ENDIAN).getFloat();
    }

    @Override
    public float float32l(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    @Override
    public void float32(byte[] a, int i, float v, boolean big) {
        ByteBuffer.wrap(a, i, 4).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putFloat(v);
    }

    @Override
    public void float32n(byte[] a, int i, float v) {
        ByteBuffer.wrap(a, i, 4).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putFloat(v);
    }

    @Override
    public void float32b(byte[] a, int i, float v) {
        ByteBuffer.wrap(a, i, 4).order(ByteOrder.BIG_ENDIAN).putFloat(v);
    }

    @Override
    public void float32l(byte[] a, int i, float v) {
        ByteBuffer.wrap(a, i, 4).order(ByteOrder.LITTLE_ENDIAN).putFloat(v);
    }

    @Override
    public double float64(byte[] a, int i, boolean big) {
        return ByteBuffer.wrap(a, i, 8).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    @Override
    public double float64n(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 8).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    @Override
    public double float64b(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 8).order(ByteOrder.BIG_ENDIAN).getDouble();
    }

    @Override
    public double float64l(byte[] a, int i) {
        return ByteBuffer.wrap(a, i, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    @Override
    public void float64(byte[] a, int i, double v, boolean big) {
        ByteBuffer.wrap(a, i, 8).order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putDouble(v);
    }

    @Override
    public void float64n(byte[] a, int i, double v) {
        ByteBuffer.wrap(a, i, 8).order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putDouble(v);
    }

    @Override
    public void float64b(byte[] a, int i, double v) {
        ByteBuffer.wrap(a, i, 8).order(ByteOrder.BIG_ENDIAN).putDouble(v);
    }

    @Override
    public void float64l(byte[] a, int i, double v) {
        ByteBuffer.wrap(a, i, 8).order(ByteOrder.LITTLE_ENDIAN).putDouble(v);
    }

    @Override
    public byte int8(ByteBuffer a, int i) {
        return a.get(i);
    }

    @Override
    public short int16(ByteBuffer a, int i, boolean big) {
        return a.order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getShort(i);
    }

    @Override
    public short int16n(ByteBuffer a, int i) {
        return a.order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getShort(i);
    }

    @Override
    public short int16b(ByteBuffer a, int i) {
        return a.order(ByteOrder.BIG_ENDIAN).getShort(i);
    }

    @Override
    public short int16l(ByteBuffer a, int i) {
        return a.order(ByteOrder.LITTLE_ENDIAN).getShort(i);
    }

    @Override
    public char uint16(ByteBuffer a, int i, boolean big) {
        return (char) a.order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getShort(i);
    }

    @Override
    public char uint16n(ByteBuffer a, int i) {
        return (char) a.order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getShort(i);
    }

    @Override
    public char uint16b(ByteBuffer a, int i) {
        return (char) a.order(ByteOrder.BIG_ENDIAN).getShort(i);
    }

    @Override
    public char uint16l(ByteBuffer a, int i) {
        return (char) a.order(ByteOrder.LITTLE_ENDIAN).getShort(i);
    }

    @Override
    public int int32(ByteBuffer a, int i, boolean big) {
        return a.order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getInt(i);
    }

    @Override
    public int int32n(ByteBuffer a, int i) {
        return a.order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getInt(i);
    }

    @Override
    public int int32b(ByteBuffer a, int i) {
        return a.order(ByteOrder.BIG_ENDIAN).getInt(i);
    }

    @Override
    public int int32l(ByteBuffer a, int i) {
        return a.order(ByteOrder.LITTLE_ENDIAN).getInt(i);
    }

    @Override
    public long int64(ByteBuffer a, int i, boolean big) {
        return a.order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getLong(i);
    }

    @Override
    public long int64n(ByteBuffer a, int i) {
        return a.order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getLong(i);
    }

    @Override
    public long int64b(ByteBuffer a, int i) {
        return a.order(ByteOrder.BIG_ENDIAN).getLong(i);
    }

    @Override
    public long int64l(ByteBuffer a, int i) {
        return a.order(ByteOrder.LITTLE_ENDIAN).getLong(i);
    }

    @Override
    public float float32(ByteBuffer a, int i, boolean big) {
        return a.order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getFloat(i);
    }

    @Override
    public float float32n(ByteBuffer a, int i) {
        return a.order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getFloat(i);
    }

    @Override
    public float float32b(ByteBuffer a, int i) {
        return a.order(ByteOrder.BIG_ENDIAN).getFloat(i);
    }

    @Override
    public float float32l(ByteBuffer a, int i) {
        return a.order(ByteOrder.LITTLE_ENDIAN).getFloat(i);
    }

    @Override
    public double float64(ByteBuffer a, int i, boolean big) {
        return a.order(big ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getDouble(i);
    }

    @Override
    public double float64n(ByteBuffer a, int i) {
        return a.order(IS_BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).getDouble(i);
    }

    @Override
    public double float64b(ByteBuffer a, int i) {
        return a.order(ByteOrder.BIG_ENDIAN).getDouble(i);
    }

    @Override
    public double float64l(ByteBuffer a, int i) {
        return a.order(ByteOrder.LITTLE_ENDIAN).getDouble(i);
    }
}