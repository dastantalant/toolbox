package com.toolbox.io.buffer.internal;

import com.toolbox.io.buffer.BufferedInput;
import com.toolbox.io.internal.LineBuffer;
import com.toolbox.io.util.ByteBufferGetUtil;
import com.toolbox.io.util.IndexCheckUtil;

import java.io.Closeable;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This could be a lot simpler if we didn't have to do pagination but ByteBuffer is limited
 * to 2 GB and direct MemorySegment access is much, much slower as of JDK 22.
 */
public final class DirectBufferedInput extends BufferedInput {

    public DirectBufferedInput(ByteBuffer bb, int pos, int lim, long totalReadLimit, MemorySegment ms, Closeable closeable, BufferedInput parent, LineBuffer linebuf) {
        super(null, pos, lim, totalReadLimit, parent, bb.order() == ByteOrder.BIG_ENDIAN, linebuf, ms, bb, closeable);
    }

    protected void copyBufferFrom(BufferedInput b) {
        super.copyBufferFrom(b);
    }

    public void prepareAndFillBuffer(int count) throws IOException {
        checkState();
        if (memorySegment != null && totalBuffered < totalReadLimit) {
            int a = available();
            long newStart = parentTotalOffset + totalBuffered - a;
            long newLen = Math.max(Math.min(totalReadLimit + parentTotalOffset - newStart, MAX_DIRECT_BUFFER_SIZE), 0);
            MemorySegment bbSegment = memorySegment.asSlice(newStart, newLen);
            byteBuffer = bbSegment.asByteBuffer().order(byteBuffer.order());
            bbStart = newStart;
            position = 0;
            limit = (int) newLen;
            totalBuffered += newLen - a;
            clampToLimit();
        }
    }

    @Override
    public int read(byte[] a, int off, int len) throws IOException {
        IndexCheckUtil.checkFromIndexSize(off, len, a.length);
        int p = fwd(len);
        ByteBufferGetUtil.get(byteBuffer, p, a, off, len);
        return len;
    }

    protected BufferedInput createEmptyView() {
        return new DirectBufferedInput(byteBuffer, 0, 0, 0L, memorySegment, null, this, lineBuffer);
    }

    @Override
    public String show() {
        return super.show() + ", bbStart=" + bbStart + ", ms.size=" + memorySegment.byteSize();
    }

    public long skip(long bytes) throws IOException {
        checkState();
        if (bytes > 0) {
            int skipAv = (int) Math.min(bytes, available());
            position += skipAv;
            long rem = bytes - skipAv;
            long remTotal = totalReadLimit - totalBuffered;
            rem = Math.min(rem, remTotal);
            if (rem > 0 && memorySegment != null) {
                long newStart = parentTotalOffset + totalBuffered + rem;
                long newLen = Math.min(totalReadLimit + parentTotalOffset - newStart, MAX_DIRECT_BUFFER_SIZE);
                byteBuffer = memorySegment.asSlice(newStart, newLen).asByteBuffer().order(byteBuffer.order());
                bbStart = newStart;
                totalBuffered += rem + newLen;
                position = 0;
                limit = (int) newLen;
                return skipAv + rem;
            } else {
                return skipAv;
            }
        } else return 0;
    }
}
