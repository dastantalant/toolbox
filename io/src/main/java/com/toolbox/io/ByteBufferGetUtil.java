package com.toolbox.io;

import java.nio.ByteBuffer;

public final class ByteBufferGetUtil {
    private ByteBufferGetUtil() {}

    public static void get(ByteBuffer buffer, int index, byte[] dst, int offset, int length) {
        checkFromIndexSize(index, length, buffer.limit());
        checkFromIndexSize(offset, length, dst.length);

        int oldPosition = buffer.position();
        try {
            buffer.position(index);
            buffer.get(dst, offset, length);
        } finally {
            buffer.position(oldPosition);
        }
    }

    public static void put(ByteBuffer buffer, int index, byte[] src, int offset, int length) {
        checkFromIndexSize(index, length, buffer.limit());
        checkFromIndexSize(offset, length, src.length);

        int oldPosition = buffer.position();
        try {
            buffer.position(index);
            buffer.put(src, offset, length);
        } finally {
            buffer.position(oldPosition);
        }
    }

    public static int getInt(ByteBuffer buffer, int index) {
        int oldPosition = buffer.position();
        try {
            buffer.position(index);
            return buffer.getInt();
        } finally {
            buffer.position(oldPosition);
        }
    }

    public static void putInt(ByteBuffer buffer, int index, int value) {
        int oldPosition = buffer.position();
        try {
            buffer.position(index);
            buffer.putInt(value);
        } finally {
            buffer.position(oldPosition);
        }
    }
    private static void checkFromIndexSize(int off, int len, int length) {
        if ((length | off | len) < 0 || len > length - off) {
            throw new IndexOutOfBoundsException();
        }
    }
}