package com.toolbox.io;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class InflaterUtil {
    private InflaterUtil() {
    }

    public static void setInput(Inflater inflater, ByteBuffer input) {
        if (input.hasArray()) {
            byte[] array = input.array();
            int offset = input.arrayOffset() + input.position();
            int length = input.remaining();
            inflater.setInput(array, offset, length);
            input.position(input.limit());
        } else {
            byte[] temp = getTempBuffer(input.remaining());
            input.get(temp, 0, input.remaining());
            inflater.setInput(temp, 0, input.remaining());
        }
    }

    public static void setInput(Deflater deflater, ByteBuffer input) {
        if (input.hasArray()) {
            byte[] array = input.array();
            int offset = input.arrayOffset() + input.position();
            int length = input.remaining();
            deflater.setInput(array, offset, length);
            input.position(input.limit());
        } else {
            byte[] temp = getTempBuffer(input.remaining());
            input.get(temp, 0, input.remaining());
            deflater.setInput(temp, 0, input.remaining());
        }
    }

    private static final ThreadLocal<byte[]> TEMP_BUFFER = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[8192];
        }
    };

    private static byte[] getTempBuffer(int size) {
        byte[] buf = TEMP_BUFFER.get();
        if (buf.length < size) {
            buf = new byte[size];
            TEMP_BUFFER.set(buf);
        }
        return buf;
    }
}