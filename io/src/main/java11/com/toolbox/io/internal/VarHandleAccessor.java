package com.toolbox.io.internal;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class VarHandleAccessor implements Accessor {

    private static final VarHandle BA_LONG_BIG = byteArrayViewVarHandle(Long.TYPE, true);
    private static final VarHandle BA_INT_BIG = byteArrayViewVarHandle(Integer.TYPE, true);
    private static final VarHandle BA_SHORT_BIG = byteArrayViewVarHandle(Short.TYPE, true);
    private static final VarHandle BA_CHAR_BIG = byteArrayViewVarHandle(Character.TYPE, true);
    private static final VarHandle BA_DOUBLE_BIG = byteArrayViewVarHandle(Double.TYPE, true);
    private static final VarHandle BA_FLOAT_BIG = byteArrayViewVarHandle(Float.TYPE, true);

    private static final VarHandle BA_LONG_LITTLE = byteArrayViewVarHandle(Long.TYPE, false);
    private static final VarHandle BA_INT_LITTLE = byteArrayViewVarHandle(Integer.TYPE, false);
    private static final VarHandle BA_SHORT_LITTLE = byteArrayViewVarHandle(Short.TYPE, false);
    private static final VarHandle BA_CHAR_LITTLE = byteArrayViewVarHandle(Character.TYPE, false);
    private static final VarHandle BA_DOUBLE_LITTLE = byteArrayViewVarHandle(Double.TYPE, false);
    private static final VarHandle BA_FLOAT_LITTLE = byteArrayViewVarHandle(Float.TYPE, false);

    private static VarHandle byteArrayViewVarHandle(Class<?> clazz, boolean flag) {
        return MethodHandles.byteArrayViewVarHandle(clazz.arrayType(), flag ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    }

    private static final VarHandle BB_LONG_BIG = byteBufferViewVarHandle(Long.TYPE, true);
    private static final VarHandle BB_INT_BIG = byteBufferViewVarHandle(Integer.TYPE, true);
    private static final VarHandle BB_SHORT_BIG = byteBufferViewVarHandle(Short.TYPE, true);
    private static final VarHandle BB_CHAR_BIG = byteBufferViewVarHandle(Character.TYPE, true);
    private static final VarHandle BB_DOUBLE_BIG = byteBufferViewVarHandle(Double.TYPE, true);
    private static final VarHandle BB_FLOAT_BIG = byteBufferViewVarHandle(Float.TYPE, true);

    private static final VarHandle BB_LONG_LITTLE = byteBufferViewVarHandle(Long.TYPE, false);
    private static final VarHandle BB_INT_LITTLE = byteBufferViewVarHandle(Integer.TYPE, false);
    private static final VarHandle BB_SHORT_LITTLE = byteBufferViewVarHandle(Short.TYPE, false);
    private static final VarHandle BB_CHAR_LITTLE = byteBufferViewVarHandle(Character.TYPE, false);
    private static final VarHandle BB_DOUBLE_LITTLE = byteBufferViewVarHandle(Double.TYPE, false);
    private static final VarHandle BB_FLOAT_LITTLE = byteBufferViewVarHandle(Float.TYPE, false);

    private static VarHandle byteBufferViewVarHandle(Class<?> clazz, boolean flag) {
        return MethodHandles.byteBufferViewVarHandle(clazz.arrayType(), flag ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    }

    VarHandleAccessor() {
    }

    public byte int8(byte[] a, int i) {
        return a[i];
    }

    public void int8(byte[] a, int i, byte v) {
        a[i] = v;
    }

    public short int16(byte[] a, int i, boolean big) {
        return (short) (big ? BA_SHORT_BIG : BA_SHORT_LITTLE).get(a, i);
    }

    public short int16n(byte[] a, int i) {
        return (short) (IS_BIG_ENDIAN ? BA_SHORT_BIG : BA_SHORT_LITTLE).get(a, i);
    }

    public short int16b(byte[] a, int i) {
        return (short) BA_SHORT_BIG.get(a, i);
    }

    public short int16l(byte[] a, int i) {
        return (short) BA_SHORT_LITTLE.get(a, i);
    }

    public void int16(byte[] a, int i, short v, boolean big) {
        (big ? BA_SHORT_BIG : BA_SHORT_LITTLE).set(a, i, v);
    }

    public void int16n(byte[] a, int i, short v) {
        (IS_BIG_ENDIAN ? BA_SHORT_BIG : BA_SHORT_LITTLE).set(a, i, v);
    }

    public void int16b(byte[] a, int i, short v) {
        BA_SHORT_BIG.set(a, i, v);
    }

    public void int16l(byte[] a, int i, short v) {
        BA_SHORT_LITTLE.set(a, i, v);
    }

    public char uint16(byte[] a, int i, boolean big) {
        return (char) (big ? BA_CHAR_BIG : BA_CHAR_LITTLE).get(a, i);
    }

    public char uint16n(byte[] a, int i) {
        return (char) (IS_BIG_ENDIAN ? BA_CHAR_BIG : BA_CHAR_LITTLE).get(a, i);
    }

    public char uint16b(byte[] a, int i) {
        return (char) BA_CHAR_BIG.get(a, i);
    }

    public char uint16l(byte[] a, int i) {
        return (char) BA_CHAR_LITTLE.get(a, i);
    }

    public void uint16(byte[] a, int i, char v, boolean big) {
        (big ? BA_CHAR_BIG : BA_CHAR_LITTLE).set(a, i, v);
    }

    public void uint16n(byte[] a, int i, char v) {
        (IS_BIG_ENDIAN ? BA_CHAR_BIG : BA_CHAR_LITTLE).set(a, i, v);
    }

    public void uint16b(byte[] a, int i, char v) {
        BA_CHAR_BIG.set(a, i, v);
    }

    public void uint16l(byte[] a, int i, char v) {
        BA_CHAR_LITTLE.set(a, i, v);
    }

    public int int32(byte[] a, int i, boolean big) {
        return (int) (big ? BA_INT_BIG : BA_INT_LITTLE).get(a, i);
    }

    public int int32n(byte[] a, int i) {
        return (int) (IS_BIG_ENDIAN ? BA_INT_BIG : BA_INT_LITTLE).get(a, i);
    }

    public int int32b(byte[] a, int i) {
        return (int) BA_INT_BIG.get(a, i);
    }

    public int int32l(byte[] a, int i) {
        return (int) BA_INT_LITTLE.get(a, i);
    }

    public void int32(byte[] a, int i, int v, boolean big) {
        (big ? BA_INT_BIG : BA_INT_LITTLE).set(a, i, v);
    }

    public void int32n(byte[] a, int i, int v) {
        (IS_BIG_ENDIAN ? BA_INT_BIG : BA_INT_LITTLE).set(a, i, v);
    }

    public void int32b(byte[] a, int i, int v) {
        BA_INT_BIG.set(a, i, v);
    }

    public void int32l(byte[] a, int i, int v) {
        BA_INT_LITTLE.set(a, i, v);
    }

    public long int64(byte[] a, int i, boolean big) {
        return (long) (big ? BA_LONG_BIG : BA_LONG_LITTLE).get(a, i);
    }

    public long int64n(byte[] a, int i) {
        return (long) (IS_BIG_ENDIAN ? BA_LONG_BIG : BA_LONG_LITTLE).get(a, i);
    }

    public long int64b(byte[] a, int i) {
        return (long) BA_LONG_BIG.get(a, i);
    }

    public long int64l(byte[] a, int i) {
        return (long) BA_LONG_LITTLE.get(a, i);
    }

    public void int64(byte[] a, int i, long v, boolean big) {
        (big ? BA_LONG_BIG : BA_LONG_LITTLE).set(a, i, v);
    }

    public void int64n(byte[] a, int i, long v) {
        (IS_BIG_ENDIAN ? BA_LONG_BIG : BA_LONG_LITTLE).set(a, i, v);
    }

    public void int64b(byte[] a, int i, long v) {
        BA_LONG_BIG.set(a, i, v);
    }

    public void int64l(byte[] a, int i, long v) {
        BA_LONG_LITTLE.set(a, i, v);
    }

    public float float32(byte[] a, int i, boolean big) {
        return (float) (big ? BA_FLOAT_BIG : BA_FLOAT_LITTLE).get(a, i);
    }

    public float float32n(byte[] a, int i) {
        return (float) (IS_BIG_ENDIAN ? BA_FLOAT_BIG : BA_FLOAT_LITTLE).get(a, i);
    }

    public float float32b(byte[] a, int i) {
        return (float) BA_FLOAT_BIG.get(a, i);
    }

    public float float32l(byte[] a, int i) {
        return (float) BA_FLOAT_LITTLE.get(a, i);
    }

    public void float32(byte[] a, int i, float v, boolean big) {
        (big ? BA_FLOAT_BIG : BA_FLOAT_LITTLE).set(a, i, v);
    }

    public void float32n(byte[] a, int i, float v) {
        (IS_BIG_ENDIAN ? BA_FLOAT_BIG : BA_FLOAT_LITTLE).set(a, i, v);
    }

    public void float32b(byte[] a, int i, float v) {
        BA_FLOAT_BIG.set(a, i, v);
    }

    public void float32l(byte[] a, int i, float v) {
        BA_FLOAT_LITTLE.set(a, i, v);
    }

    public double float64(byte[] a, int i, boolean big) {
        return (double) (big ? BA_DOUBLE_BIG : BA_DOUBLE_LITTLE).get(a, i);
    }

    public double float64n(byte[] a, int i) {
        return (double) (IS_BIG_ENDIAN ? BA_DOUBLE_BIG : BA_DOUBLE_LITTLE).get(a, i);
    }

    public double float64b(byte[] a, int i) {
        return (double) BA_DOUBLE_BIG.get(a, i);
    }

    public double float64l(byte[] a, int i) {
        return (double) BA_DOUBLE_LITTLE.get(a, i);
    }

    public void float64(byte[] a, int i, double v, boolean big) {
        (big ? BA_DOUBLE_BIG : BA_DOUBLE_LITTLE).set(a, i, v);
    }

    public void float64n(byte[] a, int i, double v) {
        (IS_BIG_ENDIAN ? BA_DOUBLE_BIG : BA_DOUBLE_LITTLE).set(a, i, v);
    }

    public void float64b(byte[] a, int i, double v) {
        BA_DOUBLE_BIG.set(a, i, v);
    }

    public void float64l(byte[] a, int i, double v) {
        BA_DOUBLE_LITTLE.set(a, i, v);
    }

    public byte int8(ByteBuffer a, int i) {
        return a.get(i);
    }

    public short int16(ByteBuffer a, int i, boolean big) {
        return (short) (big ? BB_SHORT_BIG : BB_SHORT_LITTLE).get(a, i);
    }

    public short int16n(ByteBuffer a, int i) {
        return (short) (IS_BIG_ENDIAN ? BB_SHORT_BIG : BB_SHORT_LITTLE).get(a, i);
    }

    public short int16b(ByteBuffer a, int i) {
        return (short) BB_SHORT_BIG.get(a, i);
    }

    public short int16l(ByteBuffer a, int i) {
        return (short) BB_SHORT_LITTLE.get(a, i);
    }

    public char uint16(ByteBuffer a, int i, boolean big) {
        return (char) (big ? BB_CHAR_BIG : BB_CHAR_LITTLE).get(a, i);
    }

    public char uint16n(ByteBuffer a, int i) {
        return (char) (IS_BIG_ENDIAN ? BB_CHAR_BIG : BB_CHAR_LITTLE).get(a, i);
    }

    public char uint16b(ByteBuffer a, int i) {
        return (char) BB_CHAR_BIG.get(a, i);
    }

    public char uint16l(ByteBuffer a, int i) {
        return (char) BB_CHAR_LITTLE.get(a, i);
    }

    public int int32(ByteBuffer a, int i, boolean big) {
        return (int) (big ? BB_INT_BIG : BB_INT_LITTLE).get(a, i);
    }

    public int int32n(ByteBuffer a, int i) {
        return (int) (IS_BIG_ENDIAN ? BB_INT_BIG : BB_INT_LITTLE).get(a, i);
    }

    public int int32b(ByteBuffer a, int i) {
        return (int) BB_INT_BIG.get(a, i);
    }

    public int int32l(ByteBuffer a, int i) {
        return (int) BB_INT_LITTLE.get(a, i);
    }

    public long int64(ByteBuffer a, int i, boolean big) {
        return (long) (big ? BB_LONG_BIG : BB_LONG_LITTLE).get(a, i);
    }

    public long int64n(ByteBuffer a, int i) {
        return (long) (IS_BIG_ENDIAN ? BB_LONG_BIG : BB_LONG_LITTLE).get(a, i);
    }

    public long int64b(ByteBuffer a, int i) {
        return (long) BB_LONG_BIG.get(a, i);
    }

    public long int64l(ByteBuffer a, int i) {
        return (long) BB_LONG_LITTLE.get(a, i);
    }

    public float float32(ByteBuffer a, int i, boolean big) {
        return (float) (big ? BB_FLOAT_BIG : BB_FLOAT_LITTLE).get(a, i);
    }

    public float float32n(ByteBuffer a, int i) {
        return (float) (IS_BIG_ENDIAN ? BB_FLOAT_BIG : BB_FLOAT_LITTLE).get(a, i);
    }

    public float float32b(ByteBuffer a, int i) {
        return (float) BB_FLOAT_BIG.get(a, i);
    }

    public float float32l(ByteBuffer a, int i) {
        return (float) BB_FLOAT_LITTLE.get(a, i);
    }

    public double float64(ByteBuffer a, int i, boolean big) {
        return (double) (big ? BB_DOUBLE_BIG : BB_DOUBLE_LITTLE).get(a, i);
    }

    public double float64n(ByteBuffer a, int i) {
        return (double) (IS_BIG_ENDIAN ? BB_DOUBLE_BIG : BB_DOUBLE_LITTLE).get(a, i);
    }

    public double float64b(ByteBuffer a, int i) {
        return (double) BB_DOUBLE_BIG.get(a, i);
    }

    public double float64l(ByteBuffer a, int i) {
        return (double) BB_DOUBLE_LITTLE.get(a, i);
    }
}
