package com.toolbox.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public interface Accessor {
    Accessor INSTANCE = AccessorFactory.create();

    boolean IS_BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    byte int8(byte[] a, int i);

    void int8(byte[] a, int i, byte v);

    short int16(byte[] a, int i, boolean big);

    short int16n(byte[] a, int i);

    short int16b(byte[] a, int i);

    short int16l(byte[] a, int i);

    void int16(byte[] a, int i, short v, boolean big);

    void int16n(byte[] a, int i, short v);

    void int16b(byte[] a, int i, short v);

    void int16l(byte[] a, int i, short v);

    char uint16(byte[] a, int i, boolean big);

    char uint16n(byte[] a, int i);

    char uint16b(byte[] a, int i);

    char uint16l(byte[] a, int i);

    void uint16(byte[] a, int i, char v, boolean big);

    void uint16n(byte[] a, int i, char v);

    void uint16b(byte[] a, int i, char v);

    void uint16l(byte[] a, int i, char v);

    int int32(byte[] a, int i, boolean big);

    int int32n(byte[] a, int i);

    int int32b(byte[] a, int i);

    int int32l(byte[] a, int i);

    void int32(byte[] a, int i, int v, boolean big);

    void int32n(byte[] a, int i, int v);

    void int32b(byte[] a, int i, int v);

    void int32l(byte[] a, int i, int v);

    long int64(byte[] a, int i, boolean big);

    long int64n(byte[] a, int i);

    long int64b(byte[] a, int i);

    long int64l(byte[] a, int i);

    void int64(byte[] a, int i, long v, boolean big);

    void int64n(byte[] a, int i, long v);

    void int64b(byte[] a, int i, long v);

    void int64l(byte[] a, int i, long v);

    float float32(byte[] a, int i, boolean big);

    float float32n(byte[] a, int i);

    float float32b(byte[] a, int i);

    float float32l(byte[] a, int i);

    void float32(byte[] a, int i, float v, boolean big);

    void float32n(byte[] a, int i, float v);

    void float32b(byte[] a, int i, float v);

    void float32l(byte[] a, int i, float v);

    double float64(byte[] a, int i, boolean big);

    double float64n(byte[] a, int i);

    double float64b(byte[] a, int i);

    double float64l(byte[] a, int i);

    void float64(byte[] a, int i, double v, boolean big);

    void float64n(byte[] a, int i, double v);

    void float64b(byte[] a, int i, double v);

    void float64l(byte[] a, int i, double v);

    byte int8(ByteBuffer a, int i);

    short int16(ByteBuffer a, int i, boolean big);

    short int16n(ByteBuffer a, int i);

    short int16b(ByteBuffer a, int i);

    short int16l(ByteBuffer a, int i);

    char uint16(ByteBuffer a, int i, boolean big);

    char uint16n(ByteBuffer a, int i);

    char uint16b(ByteBuffer a, int i);

    char uint16l(ByteBuffer a, int i);

    int int32(ByteBuffer a, int i, boolean big);

    int int32n(ByteBuffer a, int i);

    int int32b(ByteBuffer a, int i);

    int int32l(ByteBuffer a, int i);

    long int64(ByteBuffer a, int i, boolean big);

    long int64n(ByteBuffer a, int i);

    long int64b(ByteBuffer a, int i);

    long int64l(ByteBuffer a, int i);

    float float32(ByteBuffer a, int i, boolean big);

    float float32n(ByteBuffer a, int i);

    float float32b(ByteBuffer a, int i);

    float float32l(ByteBuffer a, int i);

    double float64(ByteBuffer a, int i, boolean big);

    double float64n(ByteBuffer a, int i);

    double float64b(ByteBuffer a, int i);

    double float64l(ByteBuffer a, int i);
}
