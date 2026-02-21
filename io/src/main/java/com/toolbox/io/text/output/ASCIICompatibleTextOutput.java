package com.toolbox.io.text.output;

import com.toolbox.io.buffer.BufferedOutput;
import com.toolbox.io.util.TextOutputUtil;
import com.toolbox.io.util.natiwe.Native;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/// TextOutput implementation for ASCII-compatible charsets. All ASCII characters are encoded as a single ASCII byte.
abstract class ASCIICompatibleTextOutput extends TextOutput {
    private static final long[] POWERS_OF_TEN = {
            1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L,
            1000000000L, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L,
            100000000000000L, 1000000000000000L, 10000000000000000L, 100000000000000000L
    };
    private static final byte[] NAN_BYTES = "NaN".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] INF_BYTES = "Infinity".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] NEG_INF_BYTES = "-Infinity".getBytes(StandardCharsets.US_ASCII);

    private final byte[] eol;
    protected final int eolLen;
    private final byte eol0;
    private final byte eol1;
    private final int maxSafeChar;

    protected ASCIICompatibleTextOutput(BufferedOutput out, Charset charset, byte[] eol) {
        super(out, charset);
        this.eol = eol;
        eolLen = eol.length;
        eol0 = eol[0];
        eol1 = eolLen > 1 ? eol[1] : (byte) 0;
        maxSafeChar = charset == StandardCharsets.ISO_8859_1 ? 255 : 127;
    }

    protected void unsafeWriteEOL(int p) {
        switch (eolLen) {
            case 1:
                getOutput().setBuffer(p, eol0);
                break;
            case 2:
                getOutput().setBuffer(p, eol0);
                getOutput().setBuffer(p + 1, eol1);
                break;
            default:
                System.arraycopy(eol, 0, getOutput().getBuffer(), p, eolLen);
        }
    }

    private void flushSurrogate() throws IOException {
        if (hasSurrogate) {
            getOutput().int8((byte) '?');
            hasSurrogate = false;
        }
    }

    protected void printRaw(String s) throws IOException {
        if (!s.isEmpty()) {
            if (hasSurrogate || Character.isHighSurrogate(s.charAt(s.length() - 1))) printRawWithSurrogates(s);
            else getOutput().append(s.getBytes(getCharset()));
        }
    }

    private void printRawWithSurrogates(String s) throws IOException {
        int len = s.length();
        int startOff = Character.isLowSurrogate(s.charAt(0)) ? 1 : 0;
        int endOff = Character.isHighSurrogate(s.charAt(len - 1)) ? 1 : 0;
        if (startOff != 0 && hasSurrogate) {
            surrogateCharBuf.put(1, s.charAt(0)).position(0);
            ByteBuffer bb = getCharset().encode(surrogateCharBuf);
            getOutput().append(bb.array(), 0, bb.limit());
        } else if (startOff != 0 || hasSurrogate) {
            getOutput().int8((byte) '?');
        }
        String v = s.substring(startOff, len - endOff);
        if (!v.isEmpty()) getOutput().append(v.getBytes(getCharset()));
        if (endOff != 0) {
            hasSurrogate = true;
            surrogateCharBuf.put(0, s.charAt(len - 1));
        } else hasSurrogate = false;
    }

    protected void printlnRaw(String s) throws IOException {
        if (!s.isEmpty()) {
            if (hasSurrogate || Character.isHighSurrogate(s.charAt(s.length() - 1))) printlnRawWithSurrogates(s);
            else {
                byte[] b = s.getBytes(getCharset());
                int l = b.length;
                int p = getOutput().fwd(l + eolLen);
                if (l > 0) System.arraycopy(b, 0, getOutput().getBuffer(), p, l);
                unsafeWriteEOL(p + l);
            }
        } else printlnRaw();
    }

    private void printlnRawWithSurrogates(String s) throws IOException {
        printRawWithSurrogates(s);
        printlnRaw();
    }

    protected void printlnRaw() throws IOException {
        flushSurrogate();
        int p = getOutput().fwd(eolLen);
        unsafeWriteEOL(p);
    }

    protected void printRawNull() throws IOException {
        flushSurrogate();
        int p = getOutput().fwd(4);
        unsafeWriteNull(p);
    }

    protected void printlnRawNull() throws IOException {
        flushSurrogate();
        int p = getOutput().fwd(4 + eolLen);
        unsafeWriteNull(p);
        unsafeWriteEOL(p + 4);
    }

    @Override
    public TextOutput print(int i) throws IOException {
        flushSurrogate();
        int len = TextOutputUtil.numChars(i);
        int p = getOutput().fwd(len);
        unsafeWriteInt(i, p, len);
        return this;
    }

    @Override
    public TextOutput println(int i) throws IOException {
        flushSurrogate();
        int len = TextOutputUtil.numChars(i);
        int p = getOutput().fwd(len + eolLen);
        unsafeWriteInt(i, p, len);
        unsafeWriteEOL(p + len);
        maybeFlush();
        return this;
    }

    @Override
    public TextOutput print(long l) throws IOException {
        flushSurrogate();
        int len = TextOutputUtil.numChars(l);
        int p = getOutput().fwd(len);
        unsafeWriteLong(l, p, len);
        return this;
    }

    @Override
    public TextOutput println(long l) throws IOException {
        flushSurrogate();
        int len = TextOutputUtil.numChars(l);
        int p = getOutput().fwd(len + eolLen);
        unsafeWriteLong(l, p, len);
        unsafeWriteEOL(p + len);
        maybeFlush();
        return this;
    }

    @Override
    public TextOutput print(boolean b) throws IOException {
        flushSurrogate();
        if (b) {
            int p = getOutput().fwd(4);
            unsafeWriteTrue(p);
        } else {
            int p = getOutput().fwd(5);
            unsafeWriteFalse(p);
        }
        return this;
    }

    @Override
    public TextOutput println(boolean b) throws IOException {
        flushSurrogate();
        if (b) {
            int p = getOutput().fwd(4 + eolLen);
            unsafeWriteTrue(p);
            unsafeWriteEOL(p + 4);
        } else {
            int p = getOutput().fwd(5 + eolLen);
            unsafeWriteFalse(p);
            unsafeWriteEOL(p + 5);
        }
        maybeFlush();
        return this;
    }

    private void unsafeWriteTrue(int p) {
        Native.INSTANCE.set(getOutput().getBuffer(), p, TextOutputUtil.LIT_TRUE);
    }

    private void unsafeWriteFalse(int p) {
        Native.INSTANCE.set(getOutput().getBuffer(), p, TextOutputUtil.LIT_FALS);
        getOutput().setBuffer(p + 4, (byte) 'e');
    }

    private void unsafeWriteNull(int p) {
        Native.INSTANCE.set(getOutput().getBuffer(), p, TextOutputUtil.LIT_NULL);
    }

    private void unsafeWriteInt(int i, int p, int len) {
        if (i < 0) getOutput().setBuffer(p, (byte) '-');
        else i = -i;
        int j = p + len - 2;
        while (i <= -100) {
            int n = i % 100;
            Native.INSTANCE.set(getOutput().getBuffer(), j, TextOutputUtil.DIGIT_PAIRS[-n]);
            j -= 2;
            i /= 100;
        }
        if (i < -9) Native.INSTANCE.set(getOutput().getBuffer(), j, TextOutputUtil.DIGIT_PAIRS[-i]);
        else getOutput().setBuffer(j + 1, (byte) (-i + '0'));
    }

    private void unsafeWriteLong(long l, int p, int len) {
        if (l < 0) getOutput().setBuffer(p, (byte) '-');
        else l = -l;
        int j = p + len - 2;
        while (l <= -100) {
            int n = (int) (l % 100);
            Native.INSTANCE.set(getOutput().getBuffer(), j, TextOutputUtil.DIGIT_PAIRS[-n]);
            j -= 2;
            l /= 100;
        }
        if (l < -9) Native.INSTANCE.set(getOutput().getBuffer(), j, TextOutputUtil.DIGIT_PAIRS[(int) -l]);
        else getOutput().setBuffer(j + 1, (byte) ((int) -l + '0'));
    }

    private void unsafeWriteDouble(double v, int precision) throws IOException {
        if (Double.isNaN(v)) {
            getOutput().append(NAN_BYTES);
            return;
        }
        if (Double.isInfinite(v)) {
            getOutput().append(v > 0 ? INF_BYTES : NEG_INF_BYTES);
            return;
        }

        if (v < 0) {
            getOutput().int8((byte) '-');
            v = -v;
        }
        if (v > Long.MAX_VALUE) {
            printRaw(String.format("%." + precision + "f", v));
            return;
        }
        int safePrecision = Math.min(Math.max(0, precision), POWERS_OF_TEN.length - 1);
        long whole = (long) v;
        long factor = POWERS_OF_TEN[safePrecision];
        long frac = Math.round((v - whole) * factor);
        if (frac >= factor) {
            whole++;
            frac = 0;
        }
        int wholeLen = TextOutputUtil.numChars(whole);
        int totalLen = wholeLen + (safePrecision > 0 ? 1 + safePrecision : 0);
        int p = getOutput().fwd(totalLen);
        byte[] buf = getOutput().getBuffer();
        long tempWhole = whole;
        int cursor = p + wholeLen;
        do {
            long q = tempWhole / 100;
            int r = (int) (tempWhole - (q * 100));
            tempWhole = q;
            buf[--cursor] = (byte) (r % 10 + '0');
            if (r >= 10 || tempWhole > 0) {
                buf[--cursor] = (byte) (r / 10 + '0');
            }
        } while (tempWhole > 0);
        unsafeWriteLong(-whole, p, wholeLen);
        if (safePrecision > 0) {
            int dotPos = p + wholeLen;
            buf[dotPos] = '.';
            long tempFrac = frac;
            int fracEnd = dotPos + 1 + safePrecision;
            for (int i = 0; i < safePrecision; i++) {
                buf[--fracEnd] = (byte) ((tempFrac % 10) + '0');
                tempFrac /= 10;
            }
        }
    }

    @Override
    public TextOutput print(char c) throws IOException {
        if (c <= maxSafeChar && !hasSurrogate) getOutput().int8((byte) c);
        else printRaw(String.valueOf(c));
        return this;
    }

    @Override
    public TextOutput println(char c) throws IOException {
        if (c <= maxSafeChar && !hasSurrogate) {
            int p = getOutput().fwd(1 + eolLen);
            getOutput().setBuffer(p, (byte) c);
            unsafeWriteEOL(p + 1);
        } else printlnRaw(String.valueOf(c));
        maybeFlush();
        return this;
    }

    @Override
    public TextOutput print(double d, int precision) throws IOException {
        flushSurrogate();
        unsafeWriteDouble(d, precision);
        return this;
    }

    @Override
    public TextOutput println(double d, int precision) throws IOException {
        flushSurrogate();
        unsafeWriteDouble(d, precision);
        printlnRaw();
        maybeFlush();
        return this;
    }

    @Override
    public TextOutput print(float f, int precision) throws IOException {
        return print((double) f, precision);
    }

    @Override
    public TextOutput println(float f, int precision) throws IOException {
        return println((double) f, precision);
    }

    @Override
    public void close() throws IOException {
        flushSurrogate();
        super.close();
    }
}

