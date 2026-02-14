package com.toolbox.core.io.internal;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BufferUtil {
    private BufferUtil() {
    }

    /// Compute a new buffer size for the given size alignment, limited to [#SOFT_MAX_ARRAY_LENGTH].
    public static int growBuffer(int current, int target, int align) {
        int l = current;
        while (l < target) {
            l *= 2;
            if (l < 0) l = Integer.MAX_VALUE;
        }
        return Math.min(l, SOFT_MAX_ARRAY_LENGTH) & -align;
    }

    // From jdk.internal.util.ArraysSupport
    public static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    public static final VarHandle BA_LONG_BIG = byteArrayViewVarHandle(Long.TYPE, true);
    public static final VarHandle BA_INT_BIG = byteArrayViewVarHandle(Integer.TYPE, true);
    public static final VarHandle BA_SHORT_BIG = byteArrayViewVarHandle(Short.TYPE, true);
    public static final VarHandle BA_CHAR_BIG = byteArrayViewVarHandle(Character.TYPE, true);
    public static final VarHandle BA_DOUBLE_BIG = byteArrayViewVarHandle(Double.TYPE, true);
    public static final VarHandle BA_FLOAT_BIG = byteArrayViewVarHandle(Float.TYPE, true);

    public static final VarHandle BA_LONG_LITTLE = byteArrayViewVarHandle(Long.TYPE, false);
    public static final VarHandle BA_INT_LITTLE = byteArrayViewVarHandle(Integer.TYPE, false);
    public static final VarHandle BA_SHORT_LITTLE = byteArrayViewVarHandle(Short.TYPE, false);
    public static final VarHandle BA_CHAR_LITTLE = byteArrayViewVarHandle(Character.TYPE, false);
    public static final VarHandle BA_DOUBLE_LITTLE = byteArrayViewVarHandle(Double.TYPE, false);
    public static final VarHandle BA_FLOAT_LITTLE = byteArrayViewVarHandle(Float.TYPE, false);

    private static VarHandle byteArrayViewVarHandle(Class<?> clazz, boolean flag) {
        return MethodHandles.byteArrayViewVarHandle(clazz.arrayType(), flag ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    }

    public static final VarHandle BB_LONG_BIG = byteBufferViewVarHandle(Long.TYPE, true);
    public static final VarHandle BB_INT_BIG = byteBufferViewVarHandle(Integer.TYPE, true);
    public static final VarHandle BB_SHORT_BIG = byteBufferViewVarHandle(Short.TYPE, true);
    public static final VarHandle BB_CHAR_BIG = byteBufferViewVarHandle(Character.TYPE, true);
    public static final VarHandle BB_DOUBLE_BIG = byteBufferViewVarHandle(Double.TYPE, true);
    public static final VarHandle BB_FLOAT_BIG = byteBufferViewVarHandle(Float.TYPE, true);

    public static final VarHandle BB_LONG_LITTLE = byteBufferViewVarHandle(Long.TYPE, false);
    public static final VarHandle BB_INT_LITTLE = byteBufferViewVarHandle(Integer.TYPE, false);
    public static final VarHandle BB_SHORT_LITTLE = byteBufferViewVarHandle(Short.TYPE, false);
    public static final VarHandle BB_CHAR_LITTLE = byteBufferViewVarHandle(Character.TYPE, false);
    public static final VarHandle BB_DOUBLE_LITTLE = byteBufferViewVarHandle(Double.TYPE, false);
    public static final VarHandle BB_FLOAT_LITTLE = byteBufferViewVarHandle(Float.TYPE, false);

    private static VarHandle byteBufferViewVarHandle(Class<?> clazz, boolean flag) {
        return MethodHandles.byteBufferViewVarHandle(clazz.arrayType(), flag ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    }

    public static MemorySegment mapReadOnlyFile(Path file) throws IOException {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size(), Arena.ofAuto());
        }
    }

    public static final int DEFAULT_BUFFER_SIZE = 32768;
    public static final byte[] EMPTY_BUFFER = new byte[0];
    public static final boolean VECTOR_ENABLED;
    public static final int VECTOR_LENGTH;

    static {
        var enabled = false;
        var vectorLen = 8; // minimum for computing a usable minimum buffer length
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


    public static VarHandle findVarHandle(MethodHandles.Lookup lookup, Class<?> clazz, String name, Class<?> type) {
        try {
            return lookup.findVarHandle(clazz, name, type);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}
