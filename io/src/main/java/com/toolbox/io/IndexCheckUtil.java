package com.toolbox.io;

public final class IndexCheckUtil {
    private IndexCheckUtil() {
    }

    public static void checkFromIndexSize(int off, int len, int length) {
        if ((length | off | len) < 0 || len > length - off) {
            throw new IndexOutOfBoundsException("Range [" + off + ", " + (off + len) + ") out of bounds for length " + length);
        }
    }

    public static void checkFromToIndex(int fromIndex, int toIndex, int length) {
        if (fromIndex < 0 || fromIndex > toIndex || toIndex > length) {
            throw new IndexOutOfBoundsException("Range [" + fromIndex + ", " + toIndex + ") out of bounds for length " + length);
        }
    }

    public static int checkIndex(int index, int length) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + length);
        }
        return index;
    }
}