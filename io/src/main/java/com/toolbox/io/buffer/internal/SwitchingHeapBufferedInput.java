package com.toolbox.io.buffer.internal;

import com.toolbox.io.adapter.BufferIterator;
import com.toolbox.io.buffer.BufferedInput;
import com.toolbox.io.util.natiwe.Native;

import java.io.IOException;

public class SwitchingHeapBufferedInput extends BufferedInput {
    protected final BufferIterator iterator;
    // bufferId1 is the currently used buffer. When there is a seam, the iterator has already been
    // advanced to the next buffer and bufferId2 holds its ID, otherwise null.
    private Object bufferId1;
    private Object bufferId2;
    // The number of bytes that have already been copied from the iterator's current buffer.
    private int seamOverlap = 0;

    public SwitchingHeapBufferedInput(BufferIterator iterator, boolean bigEndian) {
        super(Native.EMPTY_BUFFER, 0, 0, Long.MAX_VALUE, null, bigEndian, null, null, null, null);
        this.iterator = iterator;
    }

    public SwitchingHeapBufferedInput(SwitchingHeapBufferedInput parent) {
        super(null, 0, 0, 0, parent, parent.isBigEnd, null, null, null, null);
        this.iterator = parent.iterator;
    }

    @Override
    protected void copyBufferFrom(BufferedInput b) {
        super.copyBufferFrom(b);
        SwitchingHeapBufferedInput bb = (SwitchingHeapBufferedInput) b;
        seamOverlap = bb.seamOverlap;
        bufferId1 = bb.bufferId1;
        bufferId2 = bb.bufferId2;
    }

    private void updateBuffer() {
        buffer = iterator.buffer();
        position = iterator.start();
        limit = iterator.end();
        totalBuffered += (limit - position - seamOverlap);
        position += seamOverlap;
        seamOverlap = 0;
    }

    String showContent() {
        StringBuilder b = new StringBuilder("[");
        for (int i = position; i < limit; i++) {
            if (i != position) b.append(",");
            b.append(Integer.toHexString(buffer[i] & 0xff));
        }
        return b.append("]").toString();
    }

    public void prepareAndFillBuffer(int count) throws IOException {
        checkState();
        if (totalBuffered < totalReadLimit) {
            while (available() < count) {
                if (position == limit) {
                    if (seamOverlap == 0) {
                        if (bufferId1 != null) iterator.returnBuffer(bufferId1);
                        if ((bufferId1 = iterator.next()) == null) break;
                        updateBuffer();
                    } else {
                        assert (bufferId1 != null);
                        assert (bufferId2 != null);
                        iterator.returnBuffer(bufferId1);
                        bufferId1 = bufferId2;
                        bufferId2 = null;
                        updateBuffer();
                    }
                } else {
                    assert (bufferId1 != null);
                    if (position + count > buffer.length)
                        shiftOrGrowBuf(count);
                    if (seamOverlap == 0) {
                        if ((bufferId2 = iterator.next()) == null) break;
                        int rem = count - available();
                        int nlen = iterator.length();
                        if (rem < nlen) { // at least 1 byte will remain in `next` -> create a seam
                            seamOverlap = rem;
                            System.arraycopy(iterator.buffer(), iterator.start(), buffer, limit, rem);
                            limit += rem;
                            //root.next.start += rem;
                            totalBuffered += rem;
                        } else { // `next` will be exhausted -> copy without seam
                            System.arraycopy(iterator.buffer(), iterator.start(), buffer, limit, nlen);
                            limit += nlen;
                            totalBuffered += nlen;
                            iterator.returnBuffer(bufferId2);
                            bufferId2 = null;
                        }
                    } else { // existing seam
                        if (limit - position <= seamOverlap) { // pos is in the seam -> switch to next buffer
                            int a = limit - position;
                            assert (bufferId2 != null);
                            iterator.returnBuffer(bufferId1);
                            bufferId1 = bufferId2;
                            bufferId2 = null;
                            updateBuffer();
                            position -= a;
                        } else {
                            int rem = count - available();
                            int nlen = iterator.length();
                            if (rem < nlen) { // extend the current seam
                                seamOverlap += rem;
                                System.arraycopy(iterator.buffer(), iterator.start(), buffer, limit, rem);
                                limit += rem;
                                //root.next.start += rem;
                                totalBuffered += rem;
                            } else { // copy the rest of the next buffer
                                System.arraycopy(iterator.buffer(), iterator.start(), buffer, limit, nlen);
                                limit += nlen;
                                totalBuffered += nlen;
                                seamOverlap = 0;
                                iterator.returnBuffer(bufferId2);
                                bufferId2 = null;
                            }
                        }
                    }
                }
            }
            clampToLimit();
        }
    }

    protected BufferedInput createEmptyView() {
        return new SwitchingHeapBufferedInput(this);
    }
}
