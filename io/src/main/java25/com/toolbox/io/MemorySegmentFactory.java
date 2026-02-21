package com.toolbox.io;

import com.toolbox.io.buffer.BufferedInput;

import java.io.Closeable;
import java.lang.foreign.MemorySegment;

public class MemorySegmentFactory extends Factory {

    /**
     * Read data from a [MemorySegment] using the default [ByteOrder#BIG_ENDIAN]. Use
     * [MemorySegment#asSlice(long,long)] for reading only part of a segment.
     *
     * @param memorySegment Segment from which to read.
     * @param closeable     An optional [Closeable] object to close when the BufferedInput is closed, otherwise null.
     *                      This can be used to deallocate a segment which is not managed by the garbage-collector.
     */
    public Input readerOf(MemorySegment memorySegment, Closeable closeable) {
        return BufferedInput.ofMemorySegment(memorySegment, closeable);
    }

    /**
     * Same as `ofMemorySegment(memorySegment, null)`.
     *
     * @see #readerOf(MemorySegment, Closeable)
     */
    public Input readerOf(MemorySegment memorySegment) {
        return BufferedInput.ofMemorySegment(memorySegment);
    }
}
