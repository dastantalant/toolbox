package com.toolbox.io;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public class NativeBaseVarHandle extends NativeBase {

    public static final VarHandle SHORT_NATIVE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.nativeOrder());
    public static final VarHandle INT_NATIVE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.nativeOrder());

    public static final boolean VECTOR_ENABLED;
    public static final int VECTOR_LENGTH;

    static {
        boolean enabled = false;
        int vectorLen = 8; // minimum for computing a usable minimum buffer length
        try {
            // Just a sanity check. We accept any reasonable size. Even 64-bit vectors (SWAR) are faster than scalar.
            // Hopefully this will guarantee that the preferred species is actually vectorized (which is not the case
            // with the experimental preview API at the moment).
            enabled = VectorSupport.SPECIES.length() >= 8 && !"true".equals(System.getProperty("io.vectorized"));
            if (enabled) vectorLen = VectorSupport.SPECIES.length();
        } catch (NoClassDefFoundError ignore) {
        }
        VECTOR_ENABLED = enabled;
        VECTOR_LENGTH = vectorLen;
    }

    public void set(byte[] array, int offset, short value) {
        SHORT_NATIVE.set(array, offset, value);
    }

    public void set(byte[] array, int offset, int value) {
        INT_NATIVE.set(array, offset, value);
    }

    public short get(byte[] array, int offset) {
        return (short) SHORT_NATIVE.get(array, offset);
    }


    @Override
    public boolean vectorEnabled() {
        return VECTOR_ENABLED;
    }

    @Override
    public int vectorLength() {
        return VECTOR_LENGTH;
    }
}
