package com.toolbox.io;

import java.nio.ByteBuffer;

public class InternalFactory {

    public static MemoryAccessor createAccessor(byte[] array) {
        return new ByteArrayMemoryAccessor(array);
    }

    public static MemoryAccessor createAccessor(ByteBuffer buffer) {
        return new ByteBufferMemoryAccessor(buffer);
    }

    public static StringSearcher createSearcher() {
        return new ScalarStringSearcher();
    }
}