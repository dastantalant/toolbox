package com.toolbox.io.util.natiwe;

import com.toolbox.io.internal.Accessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BaseNative implements Native {

    public void set(byte[] array, int offset, short value) {
        if (Accessor.IS_BIG_ENDIAN) {
            array[offset] = (byte) (value >> 8);
            array[offset + 1] = (byte) (value);
        } else {
            array[offset] = (byte) (value);
            array[offset + 1] = (byte) (value >> 8);
        }
    }

    public void set(byte[] array, int offset, int value) {
        if (Accessor.IS_BIG_ENDIAN) {
            array[offset] = (byte) (value >> 24);
            array[offset + 1] = (byte) (value >> 16);
            array[offset + 2] = (byte) (value >> 8);
            array[offset + 3] = (byte) (value);
        } else {
            array[offset] = (byte) (value);
            array[offset + 1] = (byte) (value >> 8);
            array[offset + 2] = (byte) (value >> 16);
            array[offset + 3] = (byte) (value >> 24);
        }
    }

    public short get(byte[] array, int offset) {
        return (short) ((Accessor.IS_BIG_ENDIAN)
                ? ((array[offset] << 8) | (array[offset + 1] & 0xFF))
                : ((array[offset] & 0xFF) | (array[offset + 1] << 8)));
    }

    @Override
    public int growBuffer(int current, int target, int align) {
        int l = current;
        while (l < target) {
            l *= 2;
            if (l < 0) l = Integer.MAX_VALUE;
        }
        return Math.min(l, SOFT_MAX_ARRAY_LENGTH) & -align;
    }

    @Override
    public ByteBuffer wrap(byte[] array, int offset, int length, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.wrap(array, offset, length);
        buffer.order(order);
        return buffer;
    }

    @Override
    public ByteBuffer wrap(byte[] array, ByteOrder order) {
        return wrap(array, 0, array.length, order);
    }

    @Override
    public MappedByteBuffer mapReadOnlyFile(Path file) throws IOException {
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
    }

    @Override
    public boolean vectorEnabled() {
        return false;
    }

    @Override
    public int vectorLength() {
        return 8;
    }
}
