package com.toolbox.io;

public final class ScalarStringSearcher implements StringSearcher {
    @Override
    public long indexOf(MemoryAccessor accessor, long start, long end, byte value) {
        for (long i = start; i < end; i++) {
            if (accessor.getByte(i) == value) {
                return i;
            }
        }
        return -1;
    }
}