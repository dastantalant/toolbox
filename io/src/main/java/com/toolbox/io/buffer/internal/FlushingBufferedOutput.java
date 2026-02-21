package com.toolbox.io.buffer.internal;

import com.toolbox.io.adapter.OutputStreamAdapter;
import com.toolbox.io.buffer.BufferedOutput;
import com.toolbox.io.buffer.TopLevelBufferedOutput;

import java.io.IOException;
import java.io.OutputStream;

public final class FlushingBufferedOutput extends TopLevelBufferedOutput {
    private final OutputStream out;

    public  FlushingBufferedOutput(byte[] buf, boolean bigEndian, int start, int pos, int lim, int initialBufferSize, boolean fixed, long totalLimit, OutputStream out) {
        super(buf, bigEndian, start, pos, lim, initialBufferSize, fixed, totalLimit, null, false);
        this.out = out;
    }

    @Override
    protected void flushUpstream() throws IOException {
        out.flush();
    }

    @Override
    protected void bufferClosed(boolean closeUpstream) throws IOException {
        super.bufferClosed(closeUpstream);
        if (closeUpstream) out.close();
    }

    public void flushBlocks(boolean forceFlush) throws IOException {
        BufferedOutput b = next;
        while (true) {
            BufferedOutput bn = b.getNext();
            int blen = b.getPosition() - b.getStart();
            if (b.getState() != BufferedOutput.STATE_CLOSED) {
                if ((forceFlush && blen > 0) || !b.isFixed() && blen > getInitialBufferSize() / 2) {
                    writeToOutput(b.getBuffer(), b.getStart(), blen);
                    b.setTotalFlushed(b.getTotalFlushed()+ blen);
                    if (b.isFixed()) b.setStart(b.getPosition());
                    else {
                        b.setPosition(0);
                        b.setStart(0);
                        // `b.lim -= blen` would be the conservative choice, but we can immediately extend
                        // the limit as long as we're under the totalLimit so we don't have to leave the inner
                        // loop again to grow it later.
                        b.setLimit((int) Math.min(b.getTotalLimit() - b.getTotalFlushed(), b.getLimit()));
                    }
                }
                return;
            }
            if (b == this) {
                if (blen > 0) {
                    writeToOutput(b.getBuffer(), b.getStart(), blen);
                    totalFlushed += blen;
                }
                return;
            }
            if (blen < getInitialBufferSize() / 2 && maybeMergeToRight(b)) {
            } else if (blen > 0) writeToOutput(b.getBuffer(), b.getStart(), blen);
            b.unlinkAndReturn();
            b = bn;
        }
    }

    private boolean maybeMergeToRight(BufferedOutput b) {
        BufferedOutput n = b.getNext();
        if (!n.isFixed() && (b.getPosition() + (n.getPosition() - n.getStart()) < getInitialBufferSize())) {
            b.mergeToRight();
            return true;
        } else return false;
    }

    /// Write the buffer to the output. Must only be called on the root block.
    private void writeToOutput(byte[] buf, int off, int len) throws IOException {
        out.write(buf, off, len);
    }

    /// Return an OutputStream that directly calls methods of this BufferedOutput. Both can be used
    /// interchangeably. The OutputStream is not thread-safe. Closing the OutputStream also closes
    /// this BufferedOutput including potential parents via `close(true)`.
    public OutputStream asOutputStream() {
        return new OutputStreamAdapter(this);
    }
}
