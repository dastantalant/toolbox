package com.toolbox.io;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A ReadableBuffer contains a byte array or ByteBuffer, position & limit indices and an
 * endian flag. It provides all the methods for reading data from a single block. Operations
 * for views and lifecycle management are part of [BufferedInput].
 */
public abstract class ReadableBuffer implements Input {
    protected byte[] buffer;
    protected int position;
    protected int limit;
    protected ByteBuffer byteBuffer;
    protected boolean isBigEnd;

    /**
     * Return the number of bytes that are currently available in the buffer and can be read without
     * re-buffering or blocking.
     */
    public final int available() {
        return limit - position;
    }

    /**
     * Return the byte order of this buffer.
     */
    public final ByteOrder order() {
        return isBigEnd ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

    /**
     * Request `count` bytes to be available to read in the buffer. Less may be available if the end of the input
     * is reached. This method may change the buffer references.
     */
    public final void requestAvailable(int count) throws IOException {
        if (available() < count) prepareAndFillBuffer(count);
    }

    /**
     * Ensure that `count` bytes are available to read in the buffer, advance the buffer to the
     * position after these bytes and return the previous position. Throws [EOFException] if the end
     * of the input is reached before the requested number of bytes is available. This method may
     * change the buffer references.
     */
    public final int fwd(int count) throws IOException {
        if (available() < count) {
            prepareAndFillBuffer(count);
            if (available() < count) throw new EOFException();
        }
        int p = position;
        position += count;
        return p;
    }

    /**
     * Find the next occurrence of `b` at or after the current position and return its distance.
     * All data up to this position is guaranteed to be in the buffer afterward. Returns -1 if the
     * end of the input is reached or the maximum buffer size (a bit under 2 GB depending on
     * alignment) would be exceeded before a match is found.
     * <p>
     * This method may change the buffer references.
     */
    protected final int findNext(byte b) throws IOException {
        int i = findInRange(position, limit, b);
        if (i >= 0) return i - position;
        while (true) {
            int a = available();
            int m = Math.max(a, 32);
            int dist = Native.INSTANCE.growBuffer(m, m + 1, 1);
            if (dist <= a) return -1;
            requestAvailable(dist);
            if (available() <= a) return -1;
            i = findInRange(position + a, limit, b);
            if (i >= 0) return i - position;
        }
    }

    private int findInRange(int from, int end, byte b) {
        if (buffer != null) {
            for (int i = from; i < end; i++)
                if (Accessor.INSTANCE.int8(buffer, i) == b) return i;
        } else {
            for (int i = from; i < end; i++)
                if (Accessor.INSTANCE.int8(byteBuffer, i) == b) return i;
        }
        return -1;
    }

    /**
     * Fill the buffer as much as possible without blocking (starting at [#limit]), but at least
     * until `count` bytes are available starting at [#position] even if this requires blocking or
     * growing the buffer. Less data may only be made available when the end of the input has been
     * reached. The field [#limit] is updated accordingly.
     * <p>
     * This method may change the buffer references.
     */
    protected abstract void prepareAndFillBuffer(int count) throws IOException;

    /**
     * Read a signed 8-bit integer (`byte`).
     */
    public final byte int8() throws IOException {
        int p = fwd(1);
        return buffer != null ? Accessor.INSTANCE.int8(buffer, p) : Accessor.INSTANCE.int8(byteBuffer, p);
    }

    /**
     * Read an unsigned 8-bit integer into the lower 8 bits of an `int`.
     */
    public final int uint8() throws IOException {
        return int8() & 0xFF;
    }

    /**
     * Read a signed 16-bit integer (`short`) in the current byte [#order()].
     */
    public final short int16() throws IOException {
        int p = fwd(2);
        return buffer != null ? Accessor.INSTANCE.int16(buffer, p, isBigEnd) : Accessor.INSTANCE.int16(byteBuffer, p, isBigEnd);
    }

    /**
     * Read a signed 16-bit integer (`short`) in the native byte order.
     */
    public final short int16n() throws IOException {
        int p = fwd(2);
        return buffer != null ? Accessor.INSTANCE.int16n(buffer, p) : Accessor.INSTANCE.int16n(byteBuffer, p);
    }

    /**
     * Read a signed 16-bit integer (`short`) in big endian byte order.
     */
    public final short int16b() throws IOException {
        int p = fwd(2);
        return buffer != null ? Accessor.INSTANCE.int16b(buffer, p) : Accessor.INSTANCE.int16b(byteBuffer, p);
    }

    /**
     * Read a signed 16-bit integer (`short`) in little endian byte order.
     */
    public final short int16l() throws IOException {
        int p = fwd(2);
        return buffer != null ? Accessor.INSTANCE.int16l(buffer, p) : Accessor.INSTANCE.int16l(byteBuffer, p);
    }

    /**
     * Read an unsigned 16-bit integer (`char`) in the current byte [#order()].
     */
    public final char uint16() throws IOException {
        int p = fwd(2);
        return buffer != null ? Accessor.INSTANCE.uint16(buffer, p, isBigEnd) : Accessor.INSTANCE.uint16(byteBuffer, p, isBigEnd);
    }

    /**
     * Read an unsigned 16-bit integer (`char`) in the native byte order.
     */
    public final char uint16n() throws IOException {
        int p = fwd(2);
        return buffer != null ? Accessor.INSTANCE.uint16n(buffer, p) : Accessor.INSTANCE.uint16n(byteBuffer, p);
    }

    /**
     * Read an unsigned 16-bit integer (`char`) in big endian byte order.
     */
    public final char uint16b() throws IOException {
        int p = fwd(2);
        return buffer != null ? Accessor.INSTANCE.uint16b(buffer, p) : Accessor.INSTANCE.uint16b(byteBuffer, p);
    }

    /**
     * Read an unsigned 16-bit integer (`char`) in little endian byte order.
     */
    public final char uint16l() throws IOException {
        int p = fwd(2);
        return buffer != null ? Accessor.INSTANCE.uint16l(buffer, p) : Accessor.INSTANCE.uint16l(byteBuffer, p);
    }

    /**
     * Read a signed 32-bit integer (`int`) in the current byte [#order()].
     */
    public final int int32() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.int32(buffer, p, isBigEnd) : Accessor.INSTANCE.int32(byteBuffer, p, isBigEnd);
    }

    /**
     * Read a signed 32-bit integer (`int`) in the native byte order.
     */
    public final int int32n() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.int32n(buffer, p) : Accessor.INSTANCE.int32n(byteBuffer, p);
    }

    /**
     * Read a signed 32-bit integer (`int`) in big endian byte order.
     */
    public final int int32b() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.int32b(buffer, p) : Accessor.INSTANCE.int32b(byteBuffer, p);
    }

    /**
     * Read a signed 32-bit integer (`int`) in little endian byte order.
     */
    public final int int32l() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.int32l(buffer, p) : Accessor.INSTANCE.int32l(byteBuffer, p);
    }

    /**
     * Read an unsigned 32-bit integer into the lower 32 bits of a `long` in the current byte [#order()].
     */
    public final long uint32() throws IOException {
        return int32() & 0xFFFFFFFFL;
    }

    /**
     * Read an unsigned 32-bit integer into the lower 32 bits of a `long` in the native byte order.
     */
    public final long uint32n() throws IOException {
        return int32n() & 0xFFFFFFFFL;
    }

    /**
     * Read an unsigned 32-bit integer into the lower 32 bits of a `long` in big endian byte order.
     */
    public final long uint32b() throws IOException {
        return int32b() & 0xFFFFFFFFL;
    }

    /**
     * Read an unsigned 32-bit integer into the lower 32 bits of a `long` in little endian byte order.
     */
    public final long uint32l() throws IOException {
        return int32l() & 0xFFFFFFFFL;
    }

    /**
     * Read a signed 64-bit integer (`long`) in the current byte [#order()].
     */
    public final long int64() throws IOException {
        int p = fwd(8);
        return buffer != null ? Accessor.INSTANCE.int64(buffer, p, isBigEnd) : Accessor.INSTANCE.int64(byteBuffer, p, isBigEnd);
    }

    /**
     * Read a signed 64-bit integer (`long`) in the native byte order.
     */
    public final long int64n() throws IOException {
        int p = fwd(8);
        return buffer != null ? Accessor.INSTANCE.int64n(buffer, p) : Accessor.INSTANCE.int64n(byteBuffer, p);
    }

    /**
     * Read a signed 64-bit integer (`long`) in big endian byte order.
     */
    public final long int64b() throws IOException {
        int p = fwd(8);
        return buffer != null ? Accessor.INSTANCE.int64b(buffer, p) : Accessor.INSTANCE.int64b(byteBuffer, p);
    }

    /**
     * Read a signed 64-bit integer (`long`) in little endian byte order.
     */
    public final long int64l() throws IOException {
        int p = fwd(8);
        return buffer != null ? Accessor.INSTANCE.int64l(buffer, p) : Accessor.INSTANCE.int64l(byteBuffer, p);
    }

    /**
     * Read a 32-bit IEEE-754 floating point value (`float`) in the current byte [#order()].
     */
    public final float float32() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.float32(buffer, p, isBigEnd) : Accessor.INSTANCE.float32(byteBuffer, p, isBigEnd);
    }

    /**
     * Read a 32-bit IEEE-754 floating point value (`float`) in the native byte order.
     */
    public final float float32n() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.float32n(buffer, p) : Accessor.INSTANCE.float32n(byteBuffer, p);
    }

    /**
     * Read a 32-bit IEEE-754 floating point value (`float`) in big endian byte order.
     */
    public final float float32b() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.float32b(buffer, p) : Accessor.INSTANCE.float32b(byteBuffer, p);
    }

    /**
     * Read a 32-bit IEEE-754 floating point value (`float`) in little endian byte order.
     */
    public final float float32l() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.float32l(buffer, p) : Accessor.INSTANCE.float32l(byteBuffer, p);
    }

    /**
     * Read a 64-bit IEEE-754 floating point value (`double`) in the current byte [#order()].
     */
    public final double float64() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.float64(buffer, p, isBigEnd) : Accessor.INSTANCE.float64(byteBuffer, p, isBigEnd);
    }

    /**
     * Read a 64-bit IEEE-754 floating point value (`double`) in the native byte order.
     */
    public final double float64n() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.float64n(buffer, p) : Accessor.INSTANCE.float64n(byteBuffer, p);
    }

    /**
     * Read a 64-bit IEEE-754 floating point value (`double`) in big endian byte order.
     */
    public final double float64b() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.float64b(buffer, p) : Accessor.INSTANCE.float64b(byteBuffer, p);
    }

    /**
     * Read a 64-bit IEEE-754 floating point value (`double`) in little endian byte order.
     */
    public final double float64l() throws IOException {
        int p = fwd(4);
        return buffer != null ? Accessor.INSTANCE.float64l(buffer, p) : Accessor.INSTANCE.float64l(byteBuffer, p);
    }

    /**
     * Read the next unsigned 8-bit integer if there is more data, or return -1 if the end of the
     * input has been reached. See [java.io.InputStream#read()].
     */
    public int read() throws IOException {
        requestAvailable(1);
        if (position == limit) return -1;
        int p = position;
        position++;
        return (buffer != null ? Accessor.INSTANCE.int8(buffer, p) : Accessor.INSTANCE.int8(byteBuffer, p)) & 0xFF;
    }

    /**
     * Read up to `len` bytes into `a` starting at `off`, blocking only if the buffer is currently
     * empty. At least 1 byte is returned unless the end of the input has been reached.
     * This is equivalent to [java.io.InputStream#read(byte\[\], int, int)].
     *
     * @return the number of bytes that were read
     */
    public int readSome(byte[] a, int off, int len) throws IOException {
        IndexCheckUtil.checkFromIndexSize(off, len, a.length);
        requestAvailable(1);
        if (position == limit) return 0;
        int l = Math.min(len, available());
        if (buffer != null) System.arraycopy(buffer, position, a, off, l);
        else ByteBufferGetUtil.get(byteBuffer, position, a, off, l);
        return l;
    }

    /**
     * Read up to `len` bytes into `a` starting at `off`, blocking until all requested data is
     * available. Less than `len` bytes can only be returned if the end of the input is reached.
     * This is equivalent to [java.io.InputStream#readNBytes(byte\[\], int, int)()].
     *
     * @return the number of bytes that were read
     */
    public int read(byte[] a, int off, int len) throws IOException {
        IndexCheckUtil.checkFromIndexSize(off, len, a.length);
        int l0 = len;
        while (len > 0) {
            requestAvailable(1);
            if (available() == 0) break;
            int l = Math.min(len, available());
            if (l > 0) {
                if (buffer != null) System.arraycopy(buffer, position, a, off, l);
                else ByteBufferGetUtil.get(byteBuffer, position, a, off, l);
                position += l;
                off += l;
                len -= l;
            }
        }
        return l0 - len;
    }

    @Override
    public byte readByte() throws IOException {
        return int8();
    }

    @Override
    public short readShort() throws IOException {
        return int16();
    }

    @Override
    public int readInt() throws IOException {
        return int32();
    }

    @Override
    public long readLong() throws IOException {
        return int64();
    }

    @Override
    public float readFloat() throws IOException {
        return float32();
    }

    @Override
    public double readDouble() throws IOException {
        return float64();
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public byte getBuffer(int index) {
        return buffer[index];
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public void setBuffer(int index, byte byt) {
        buffer[index] = byt;
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

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public boolean isBigEnd() {
        return isBigEnd;
    }

    public void setBigEnd(boolean bigEnd) {
        isBigEnd = bigEnd;
    }
}
