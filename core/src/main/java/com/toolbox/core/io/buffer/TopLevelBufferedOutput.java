package com.toolbox.core.io.buffer;

import java.io.IOException;

public abstract class TopLevelBufferedOutput extends BufferedOutput {
    private final int initialBufferSize;

    public  TopLevelBufferedOutput(byte[] buf, boolean bigEndian, int start, int pos, int lim, int initialBufferSize, boolean fixed, long totalLimit, TopLevelBufferedOutput cache, boolean preferSplit) {
        super(buf, bigEndian, start, pos, lim, fixed, totalLimit, null, cache, preferSplit);
        this.initialBufferSize = initialBufferSize;
    }

    private NestedBufferedOutput cachedExclusive;
    private NestedBufferedOutput cachedShared = null;

    /// Flush and unlink all closed blocks and optionally flush the root block.
    public abstract void flushBlocks(boolean forceFlush) throws IOException;

   protected void bufferClosed(boolean closeUpstream) throws IOException {
        cachedExclusive = null;
        cachedShared = null;
    }

    public void returnToCache(NestedBufferedOutput b) {
        if (!b.isNocache()) {
            b.topLevel = null;
            b.rootBlock = null;
            if (b.sharing == SHARING_LEFT) {
                b.setBuffer(null);
                b.next = cachedShared;
                cachedShared = b;
            } else {
                b.next = cachedExclusive;
                cachedExclusive = b;
            }
        }
    }

    /// Get a cached or new exclusive block.
    public NestedBufferedOutput getExclusiveBlock() {
        if (cachedExclusive == null) {
            return new NestedBufferedOutput(new byte[initialBufferSize], false, this);
        } else {
            var b = cachedExclusive;
            cachedExclusive = (NestedBufferedOutput) b.next;
            return b;
        }
    }

    /// Get a cached or new shared block.
    NestedBufferedOutput getSharedBlock() {
        if (cachedShared == null) {
            return new NestedBufferedOutput(null, true, this);
        } else {
            var b = cachedShared;
            cachedShared = (NestedBufferedOutput) b.next;
            return b;
        }
    }

    public int getInitialBufferSize() {
        return initialBufferSize;
    }
}
