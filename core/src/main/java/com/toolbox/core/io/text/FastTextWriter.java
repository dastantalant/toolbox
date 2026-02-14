package com.toolbox.core.io.text;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Deprecated
public class FastTextWriter implements Closeable, Flushable {
    private static final int[] INT_SIZE_TABLE = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

    private static final long[] LONG_SIZE_TABLE = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999,
            9999999999L, 99999999999L, 999999999999L, 9999999999999L, 99999999999999L, 999999999999999L,
            9999999999999999L, 99999999999999999L, 999999999999999999L, Long.MAX_VALUE};

    private static final byte[] DIGIT_TENS = new byte[100];
    private static final byte[] DIGIT_ONES = new byte[100];

    static {
        for (int i = 0; i < 100; i++) {
            DIGIT_TENS[i] = (byte) (i / 10 + '0');
            DIGIT_ONES[i] = (byte) (i % 10 + '0');
        }
    }

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final byte MINUS = (byte) '-';
    private static final byte ZERO = (byte) '0';
    private static final byte DOT = (byte) '.';
    private static final byte[] TRUE_BYTES = "true".getBytes(StandardCharsets.ISO_8859_1);
    private static final byte[] FALSE_BYTES = "false".getBytes(StandardCharsets.ISO_8859_1);
    private static final byte[] NULL_BYTES = "null".getBytes(StandardCharsets.ISO_8859_1);
    private static final byte[] NEW_LINE_BYTES = System.lineSeparator().getBytes(StandardCharsets.ISO_8859_1);
    private static final long[] POWERS_OF_TEN = {1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 1000000000L};

    private final OutputStream out;
    private final byte[] buf;
    private final int capacity;
    private int pos = 0;

    public FastTextWriter(OutputStream out) {
        this(out, DEFAULT_BUFFER_SIZE);
    }

    public FastTextWriter(OutputStream out, int bufferSize) {
        this.out = out;
        capacity = bufferSize;
        buf = new byte[bufferSize];
    }

    @Override
    public void flush() throws IOException {
        if (pos > 0) {
            out.write(buf, 0, pos);
            pos = 0;
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }

    private void ensureCapacity(int required) throws IOException {
        if (pos + required >= capacity) {
            flush();
        }
    }

    public FastTextWriter append(char c) throws IOException {
        if (pos >= capacity) flush();
        buf[pos++] = (byte) c;
        return this;
    }

    public FastTextWriter append(byte b) throws IOException {
        if (pos >= capacity) flush();
        buf[pos++] = b;
        return this;
    }

    public FastTextWriter append(Enum e) throws IOException {
        append(e.name());
        return this;
    }

    public FastTextWriter append(String s) throws IOException {
        if (s == null) return writeRawBytes(NULL_BYTES);
        int len = s.length();
        int off = 0;

        while (off < len) {
            int chunk = Math.min(capacity - pos, len - off);
            if (chunk == 0) {
                flush();
                continue;
            }
            for (int i = 0; i < chunk; i++) {
                buf[pos++] = (byte) s.charAt(off + i);
            }
            off += chunk;
        }
        return this;
    }

    public FastTextWriter append(int i) throws IOException {
        if (i == Integer.MIN_VALUE) return append("-2147483648");
        ensureCapacity(11);
        if (i < 0) {
            buf[pos++] = MINUS;
            i = -i;
        }

        if (i < 10) {
            buf[pos++] = (byte) (i + ZERO);
            return this;
        }

        int size = stringSize(i);
        int charPos = pos + size;
        pos = charPos;

        while (i >= 100) {
            int q = i / 100;
            int r = i - (q * 100);
            i = q;
            buf[--charPos] = DIGIT_ONES[r];
            buf[--charPos] = DIGIT_TENS[r];
        }

        buf[--charPos] = DIGIT_ONES[i];
        if (i >= 10) {
            buf[--charPos] = DIGIT_TENS[i];
        }

        return this;
    }

    public FastTextWriter append(short s) throws IOException {
        return append((int) s);
    }

    public FastTextWriter append(long i) throws IOException {
        if (i == Long.MIN_VALUE) {
            return append("-9223372036854775808");
        }

        ensureCapacity(21);

        if (i < 0) {
            buf[pos++] = MINUS;
            i = -i;
        }

        int size = stringSize(i);
        int charPos = pos + size;
        pos = charPos;

        while (i > Integer.MAX_VALUE) {
            long q = i / 100;
            int r = (int) (i - (q * 100));
            i = q;
            buf[--charPos] = DIGIT_ONES[r];
            buf[--charPos] = DIGIT_TENS[r];
        }

        int i2 = (int) i;

        while (i2 >= 100) {
            int q = i2 / 100;
            int r = i2 - (q * 100);
            i2 = q;
            buf[--charPos] = DIGIT_ONES[r];
            buf[--charPos] = DIGIT_TENS[r];
        }

        buf[--charPos] = DIGIT_ONES[i2];
        if (i2 >= 10) {
            buf[--charPos] = DIGIT_TENS[i2];
        }

        return this;
    }

    public FastTextWriter append(double val) throws IOException {
        append(val, 2);
        return this;
    }

    public FastTextWriter append(double val, int precision) throws IOException {
        if (Double.isNaN(val)) return append("NaN");
        if (Double.isInfinite(val)) return append(val > 0 ? "Infinity" : "-Infinity");

        if (val < 0) {
            ensureCapacity(1);
            buf[pos++] = MINUS;
            val = -val;
        }
        long factor = POWERS_OF_TEN[Math.min(precision, POWERS_OF_TEN.length - 1)];

        long whole = (long) val;
        long frac = Math.round((val - whole) * factor);

        if (frac >= factor) {
            whole++;
            frac = 0;
        }

        append(whole);

        if (precision > 0) {
            ensureCapacity(1 + precision);
            buf[pos++] = DOT;
            long v = factor + frac;
            int len = precision;
            int charPos = pos + len;
            pos = charPos;

            while (len-- > 0) {
                long q = v / 10;
                int r = (int) (v - (q * 10));
                v = q;
                buf[--charPos] = (byte) (r + '0');
            }
        }
        return this;
    }

    public FastTextWriter append(float val, int precision) throws IOException {
        return append((double) val, precision);
    }

    public FastTextWriter append(boolean b) throws IOException {
        return writeRawBytes(b ? TRUE_BYTES : FALSE_BYTES);
    }

    private FastTextWriter writeRawBytes(byte[] src) throws IOException {
        ensureCapacity(src.length);
        System.arraycopy(src, 0, buf, pos, src.length);
        pos += src.length;
        return this;
    }

    private int stringSize(int x) {
        for (int i = 0; ; i++) if (x <= INT_SIZE_TABLE[i]) return i + 1;
    }

    private int stringSize(long x) {
        for (int i = 0; ; i++) if (x <= LONG_SIZE_TABLE[i]) return i + 1;
    }

    public FastTextWriter newLine() throws IOException {
        return writeRawBytes(NEW_LINE_BYTES);
    }
}