package com.dastan.core.io.buffer;

import com.dastan.core.io.Output;
import com.dastan.core.io.internal.Accessor;
import com.dastan.core.io.text.TextOutput;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


/// A WritableBuffer contains a byte array, start/position/limit indices and an endian flag.
/// It provides all the methods for writing data to a single block. Operations that can span
/// multiple blocks (like creating views or flushing data) are part of [BufferedOutput].
///
/// @param <S> The self-type that is returned by most methods for chaining operations.
public abstract class WritableBuffer<S extends WritableBuffer<S>> implements Output {
    protected byte[] buffer;
    protected boolean isBigEnd;
    protected int start;
    protected int position;
    protected int limit;

    /// Return the byte order of this buffer.
    public final ByteOrder order() {
        return isBigEnd ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

    public final int available() {
        return limit - position;
    }

    /// Advance the position in the buffer by `count` and return the previous position.
    /// Throws an IOException if the buffer's size limit would be exceeded.
    public final int fwd(int count) throws IOException {
        ensureAvailable(count);
        var p = position;
        position += count;
        return p;
    }

    /// Ensure `count` bytes are available in the buffer without advancing the position.
    public final void ensureAvailable(int count) throws IOException {
        // The goal here is to allow HotSpot to elide range checks in some scenarios (e.g.
        // `BufferedOutputUncheckedBenchmark`). The condition should be `available() < count`,
        // i.e. `lim - pos < count` but this does not elide range checks. `pos + count > lim` does,
        // but it would produce an incorrect result due to signed arithmetic if `pos + count`
        // overflows. Using the `Integer.compareUnsigned` intrinsic does not elide the range checks.
        // The double condition does, with no performance impact in the benchmarks.
        if (position + count > limit || position + count < 0) {
            flushAndGrow(count);
            if (position + count > limit || position + count < 0) throw new EOFException();
        }
    }

    /// Try to advance the position by `count` and return the previous position. May advance by less
    /// (or even 0) if the buffer's size limit would be exceeded. The caller must check [#available()].
    public final int tryFwd(int count) throws IOException {
        if (position + count > limit || position + count < 0) flushAndGrow(count);
        var p = position;
        position += Math.min(count, available());
        return p;
    }

    @Override
    public final WritableBuffer<S> append(int b) throws IOException {
        // int8((byte)b) уже есть в WritableBuffer.
        // Он сдвинет позицию (pos++) и проверит место в буфере.
        return int8((byte) b);
    }

    /// Write the contents of the given array region.
    @Override
    public final WritableBuffer<S> append(byte[] a, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, a.length);
        var p = fwd(len);
        System.arraycopy(a, off, buffer, p, len);
        return this;
    }

    /// Write the contents of the given array.
    @Override
    public final WritableBuffer<S> append(byte[] a) throws IOException {
        return append(a, 0, a.length);
    }

    /// Write a signed 8-bit integer (`byte`).
    public final WritableBuffer<S> int8(byte b) throws IOException {
        var p = fwd(1);
        Accessor.INSTANCE.int8(buffer, p, b);
        return this;
    }

    /// Write the lower 8 bits of the given `int` as an unsigned 8-bit integer.
    public final WritableBuffer<S> uint8(int b) throws IOException {
        return int8((byte) b);
    }

    /// Write a signed 16-bit integer (`short`) in the current byte [#order()].
    public final WritableBuffer<S> int16(short s) throws IOException {
        var p = fwd(2);
        Accessor.INSTANCE.int16(buffer, p, s, isBigEnd);
        return this;
    }

    /// Write a signed 16-bit integer (`short`) in the native byte order.
    public final WritableBuffer<S> int16n(short s) throws IOException {
        var p = fwd(2);
        Accessor.INSTANCE.int16n(buffer, p, s);
        return this;
    }

    /// Write a signed 16-bit integer (`short`) in big endian byte order.
    public final WritableBuffer<S> int16b(short s) throws IOException {
        var p = fwd(2);
        Accessor.INSTANCE.int16b(buffer, p, s);
        return this;
    }

    /// Write a signed 16-bit integer (`short`) in little endian byte order.
    public final WritableBuffer<S> int16l(short s) throws IOException {
        var p = fwd(2);
        Accessor.INSTANCE.int16l(buffer, p, s);
        return this;
    }

    /// Write an unsigned 16-bit integer (`char`) in the current byte [#order()].
    public final WritableBuffer<S> uint16(char c) throws IOException {
        var p = fwd(2);
        Accessor.INSTANCE.uint16(buffer, p, c, isBigEnd);
        return this;
    }

    /// Write an unsigned 16-bit integer (`char`) in the native byte order.
    public final WritableBuffer<S> uint16n(char c) throws IOException {
        var p = fwd(2);
        Accessor.INSTANCE.uint16n(buffer, p, c);
        return this;
    }

    /// Write an unsigned 16-bit integer (`char`) in big endian byte order.
    public final WritableBuffer<S> uint16b(char c) throws IOException {
        var p = fwd(2);
        Accessor.INSTANCE.uint16b(buffer, p, c);
        return this;
    }

    /// Write an unsigned 16-bit integer (`char`) in little endian byte order.
    public final WritableBuffer<S> uint16l(char c) throws IOException {
        var p = fwd(2);
        Accessor.INSTANCE.uint16l(buffer, p, c);
        return this;
    }

    /// Write a signed 32-bit integer (`int`) in the current byte [#order()].
    public final WritableBuffer<S> int32(int i) throws IOException {
        var p = fwd(4);
        Accessor.INSTANCE.int32(buffer, p, i, isBigEnd);
        return this;
    }

    /// Write a signed 32-bit integer (`int`) in the native byte order.
    public final WritableBuffer<S> int32n(int i) throws IOException {
        var p = fwd(4);
        Accessor.INSTANCE.int32n(buffer, p, i);
        return this;
    }

    /// Write a signed 32-bit integer (`int`) in big endian byte order.
    public final WritableBuffer<S> int32b(int i) throws IOException {
        var p = fwd(4);
        Accessor.INSTANCE.int32b(buffer, p, i);
        return this;
    }

    /// Write a signed 32-bit integer (`int`) in little endian byte order.
    public final WritableBuffer<S> int32l(int i) throws IOException {
        var p = fwd(4);
        Accessor.INSTANCE.int32l(buffer, p, i);
        return this;
    }

    /// Write the lower 32 bits of the given `long` as an unsigned 32-bit integer in the current byte [#order()].
    public final WritableBuffer<S> uint32(long i) throws IOException {
        return int32((int) i);
    }

    /// Write the lower 32 bits of the given `long` as an unsigned 32-bit integer in the native byte order.
    public final WritableBuffer<S> uint32n(long i) throws IOException {
        return int32n((int) i);
    }

    /// Write the lower 32 bits of the given `long` as an unsigned 32-bit integer in big endian byte order.
    public final WritableBuffer<S> uint32b(long i) throws IOException {
        return int32b((int) i);
    }

    /// Write the lower 32 bits of the given `long` as an unsigned 32-bit integer in little endian byte order.
    public final WritableBuffer<S> uint32l(long i) throws IOException {
        return int32l((int) i);
    }

    /// Write a signed 64-bit integer (`long`) in the current byte [#order()].
    public final WritableBuffer<S> int64(long l) throws IOException {
        var p = fwd(8);
        Accessor.INSTANCE.int64(buffer, p, l, isBigEnd);
        return this;
    }

    /// Write a signed 64-bit integer (`long`) in the native byte order.
    public final WritableBuffer<S> int64n(long l) throws IOException {
        var p = fwd(8);
        Accessor.INSTANCE.int64n(buffer, p, l);
        return this;
    }

    /// Write a signed 64-bit integer (`long`) in big endian byte order.
    public final WritableBuffer<S> int64b(long l) throws IOException {
        var p = fwd(8);
        Accessor.INSTANCE.int64b(buffer, p, l);
        return this;
    }

    /// Write a signed 64-bit integer (`long`) in little endian byte order.
    public final WritableBuffer<S> int64l(long l) throws IOException {
        var p = fwd(8);
        Accessor.INSTANCE.int64l(buffer, p, l);
        return this;
    }

    /// Write a 32-bit IEEE-754 floating point value (`float`) in the current byte [#order()].
    public final WritableBuffer<S> float32(float f) throws IOException {
        var p = fwd(4);
        Accessor.INSTANCE.float32(buffer, p, f, isBigEnd);
        return this;
    }

    /// Write a 32-bit IEEE-754 floating point value (`float`) in the native byte order.
    public final WritableBuffer<S> float32n(float f) throws IOException {
        var p = fwd(4);
        Accessor.INSTANCE.float32n(buffer, p, f);
        return this;
    }

    /// Write a 32-bit IEEE-754 floating point value (`float`) in big endian byte order.
    public final WritableBuffer<S> float32b(float f) throws IOException {
        var p = fwd(4);
        Accessor.INSTANCE.float32b(buffer, p, f);
        return this;
    }

    /// Write a 32-bit IEEE-754 floating point value (`float`) in little endian byte order.
    public final WritableBuffer<S> float32l(float f) throws IOException {
        var p = fwd(4);
        Accessor.INSTANCE.float32l(buffer, p, f);
        return this;
    }

    /// Write a 64-bit IEEE-754 floating point value (`double`) in the current byte [#order()].
    public final WritableBuffer<S> float64(double d) throws IOException {
        var p = fwd(8);
        Accessor.INSTANCE.float64(buffer, p, d, isBigEnd);
        return this;
    }

    /// Write a 64-bit IEEE-754 floating point value (`double`) in the native byte order.
    public final WritableBuffer<S> float64n(double d) throws IOException {
        var p = fwd(8);
        Accessor.INSTANCE.float64n(buffer, p, d);
        return this;
    }

    /// Write a 64-bit IEEE-754 floating point value (`double`) in big endian byte order.
    public final WritableBuffer<S> float64b(double d) throws IOException {
        var p = fwd(8);
        Accessor.INSTANCE.float64b(buffer, p, d);
        return this;
    }

    /// Write a 64-bit IEEE-754 floating point value (`double`) in little endian byte order.
    public final WritableBuffer<S> float64l(double d) throws IOException {
        var p = fwd(8);
        Accessor.INSTANCE.float64l(buffer, p, d);
        return this;
    }

    /// Write a [String] in the given [Charset].
    ///
    /// Encoding is performed as a standalone operation with the default replacement characters. It is not possible
    /// to write surrogate pairs that are split across 2 strings. Use [TextOutput] if you need this feature.
    ///
    /// The encoded data is written verbatim without a length prefix or terminator.
    public final int string(String string, Charset charset) throws IOException {
        if (string.isEmpty()) return 0;
        if (charset == StandardCharsets.UTF_8) {
            return stringUtf8(string);
        } else if (charset == StandardCharsets.ISO_8859_1 || charset == StandardCharsets.US_ASCII) {
            return stringLatin1(string, 0);
        }
        return stringFallback(string, charset, 0);
    }

    /// Write a [String] in UTF-8 encoding.
    ///
    /// Note that this is the standard UTF-8 format. It is not compatible with [java.io.DataInput] /
    /// [java.io.DataOutput] which use a non-standard variant.
    ///
    /// @see #string(String, Charset)
    public final int string(String string) throws IOException {
        return stringUtf8(string);
    }

    private int stringFallback(String string, Charset charset, int extra) throws IOException {
        byte[] b = string.getBytes(charset);
        int p = fwd(b.length + extra);
        System.arraycopy(b, 0, buffer, p, b.length);
        return b.length + extra;
    }

    private int stringLatin1(String string, int extra) throws IOException {
        int len = string.length();
        int p = fwd(len + extra);
        for (int i = 0; i < len; i++) {
            Accessor.INSTANCE.int8(buffer, p + i, (byte) string.charAt(i));
        }
        return len + extra;
    }

    private int stringUtf8(String string) throws IOException {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        int p = fwd(bytes.length);
        System.arraycopy(bytes, 0, buffer, p, bytes.length);
        return bytes.length;
    }

    public final int zString(String string, Charset charset) throws IOException {
        int l = string(string, charset);
        int8((byte) 0);
        return l + 1;
    }

    public final int zString(String string) throws IOException {
        return zString(string, StandardCharsets.UTF_8);
    }

    @Override
    public WritableBuffer<S> append(byte b) throws IOException {
        return int8(b);
    }

    @Override
    public WritableBuffer<S> append(short s) throws IOException {
        return int16(s);
    }

    @Override
    public WritableBuffer<S> append(long l) throws IOException {
        return int64(l);
    }

    @Override
    public WritableBuffer<S> append(float f) throws IOException {
        return float32(f);
    }

    @Override
    public WritableBuffer<S> append(double d) throws IOException {
        return float64(d);
    }

    @Override
    public WritableBuffer<S> append(String s, Charset charset) throws IOException {
        string(s, charset);
        return this;
    }

    @Override
    public WritableBuffer<S> append(String s) throws IOException {
        string(s);
        return this;
    }

    @Override
    public WritableBuffer<S> append(CharSequence sequence) throws IOException {
        if (sequence == null) return appendNull();
        if (sequence instanceof String s) return append(s);
        int len = sequence.length();
        for (int i = 0; i < len; i++) {
            append(sequence.charAt(i));
        }
        return this;
    }

    @Override
    public WritableBuffer<S> append(CharSequence sequence, int start, int end) throws IOException {
        if (sequence == null) return appendNull();
        if (sequence instanceof String s) {
            return append(s.substring(start, end));
        }
        for (int i = start; i < end; i++) {
            append(sequence.charAt(i));
        }
        return this;
    }

    @Override
    public WritableBuffer<S>  append(char c) throws IOException {
        if (c < 0x80) {
            if (position >= limit) {
                flushAndGrow(1);
            }
            buffer[position++] = (byte) c;
        } else if (c < 0x800) {
            ensureAvailable(2);
            buffer[position++] = (byte) (0xC0 | (c >> 6));
            buffer[position++] = (byte) (0x80 | (c & 0x3F));
        } else if (Character.isSurrogate(c)) {
            text().print(c);
        } else {
            ensureAvailable(3);
            buffer[position++] = (byte) (0xE0 | (c >> 12));
            buffer[position++] = (byte) (0x80 | ((c >> 6) & 0x3F));
            buffer[position++] = (byte) (0x80 | (c & 0x3F));
        }
        return this;
    }

    @Override
    public WritableBuffer<S> append(boolean b) throws IOException {
        if (b) {
            ensureAvailable(4);
            buffer[position++] = 't';
            buffer[position++] = 'r';
            buffer[position++] = 'u';
            buffer[position++] = 'e';
        } else {
            ensureAvailable(5);
            buffer[position++] = 'f';
            buffer[position++] = 'a';
            buffer[position++] = 'l';
            buffer[position++] = 's';
            buffer[position++] = 'e';
        }
        return this;
    }

    @Override
    public WritableBuffer<S> append(Enum<?> e) throws IOException {
        return e == null ? append("null") : append(e.name());
    }

    @Override
    public WritableBuffer<S> newLine() throws IOException {
        return append('\n');
    }

    @Override
    public WritableBuffer<S> appendNull() throws IOException {
        ensureAvailable(4);
        buffer[position++] = 'n';
        buffer[position++] = 'u';
        buffer[position++] = 'l';
        buffer[position++] = 'l';
        return this;
    }

    protected abstract void flushAndGrow(int count) throws IOException;

    public byte[] getBuffer() {
        return buffer;
    }

    public byte getBuffer(int index) {
        return buffer[index];
    }

    public void setBuffer(int index, byte b) {
        buffer[index] = b;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public boolean isBigEnd() {
        return isBigEnd;
    }

    public void setBigEnd(boolean bigEnd) {
        isBigEnd = bigEnd;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
