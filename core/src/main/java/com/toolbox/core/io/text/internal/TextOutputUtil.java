package com.toolbox.core.io.text.internal;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public class TextOutputUtil {
    private TextOutputUtil() {
    }

    private static final boolean IS_BIG_END = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    public static final VarHandle SHORT_NATIVE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.nativeOrder());
    public static final VarHandle INT_NATIVE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.nativeOrder());

    public static final int LIT_TRUE = lit4("true");
    public static final int LIT_FALS = lit4("fals");
    public static final int LIT_NULL = lit4("null");

    private static int lit4(String s) {
        var d0 = (int) s.charAt(0);
        var d1 = (int) s.charAt(1);
        var d2 = (int) s.charAt(2);
        var d3 = (int) s.charAt(3);
        return IS_BIG_END
                ? (d0 << 24) | (d1 << 16) | (d2 << 8) | d3
                : (d3 << 24) | (d2 << 16) | (d1 << 8) | d0;
    }

    /// ASCII decimal digits of numbers 0 to 99 encoded as byte pairs in native byte order.
    public static final short[] DIGIT_PAIRS = new short[100];

    static {
        for (var i = 0; i < 100; i++) {
            var d0 = (i % 10) + '0';
            var d1 = ((i / 10) % 10) + '0';
            var c = IS_BIG_END ? (d1 << 8) | d0 : (d0 << 8) | d1;
            DIGIT_PAIRS[i] = (short) c;
        }
    }

    /// Number of chars needed to represent the given Int value using the same algorithm as `Integer.stringSize` (which
    /// is not public API). This tends to be fastest for small to medium-sized numbers. Manually unrolling
    /// (`if(x >= -9) ... else if(x >= -99) ...`) is faster for very small numbers, a constant-time algorithm like
    /// https://github.com/ramanawithu/fast_int_to_string/blob/7a2d82bb4aea91afab48b741e87460f810141c71/fast_int_to_string.hpp#L47
    /// may be faster for completely random numbers.
    public static int numChars(int x) {
        int d = 1;
        if (x >= 0) {
            x = -x;
            d = 0;
        }
        var p = -10;
        for (var i = 1; i < 10; i++) {
            if (x > p) return d + i;
            p *= 10;
        }
        return d + 10;
    }

    public static int numChars(long x) {
        int d = 1;
        if (x >= 0) {
            x = -x;
            d = 0;
        }
        var p = -10L;
        for (var i = 1; i < 19; i++) {
            if (x > p) return d + i;
            p *= 10;
        }
        return d + 19;
    }
}
