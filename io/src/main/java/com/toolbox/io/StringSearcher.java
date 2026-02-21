package com.toolbox.io;

public interface StringSearcher {
    long indexOf(MemoryAccessor accessor, long start, long end, byte value);
}