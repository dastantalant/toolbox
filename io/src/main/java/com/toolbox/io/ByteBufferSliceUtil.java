package com.toolbox.io;

import java.nio.ByteBuffer;

public final class ByteBufferSliceUtil {
    private ByteBufferSliceUtil() {}

    public static ByteBuffer slice(ByteBuffer buffer, int index, int length) {
        checkFromIndexSize(index, length, buffer.capacity());
        int oldPosition = buffer.position();
        int oldLimit = buffer.limit();
        try {
            buffer.position(index);
            buffer.limit(index + length);
            return buffer.slice();
        } finally {
            buffer.position(oldPosition);
            buffer.limit(oldLimit);
        }
    }

    public static ByteBuffer sliceDestructive(ByteBuffer buffer, int index, int length) {
        checkFromIndexSize(index, length, buffer.capacity());
        buffer.position(index);
        buffer.limit(index + length);
        ByteBuffer sliced = buffer.slice();
        buffer.limit(buffer.capacity());
        return sliced;
    }

    public static ByteBuffer sliceThreadSafe(ByteBuffer buffer, int index, int length) {
        checkFromIndexSize(index, length, buffer.capacity());
        ByteBuffer dup = buffer.duplicate();
        dup.position(index);
        dup.limit(index + length);
        return dup.slice();
    }

    private static void checkFromIndexSize(int off, int len, int length) {
        if ((length | off | len) < 0 || len > length - off) {
            throw new IndexOutOfBoundsException("Range [" + off + ", " + (off + len) + ") out of bounds for length " + length);
        }
    }
}