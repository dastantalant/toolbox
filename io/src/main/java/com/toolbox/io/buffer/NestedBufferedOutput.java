package com.toolbox.io.buffer;

import java.io.IOException;

public final class NestedBufferedOutput extends BufferedOutput {
    /**
     * The parent from which this block was created by [#defer()], otherwise null.
     */
    private BufferedOutput parent = null;

    private boolean nocache;

    public NestedBufferedOutput(byte[] buf, boolean fixed, TopLevelBufferedOutput topLevel) {
        super(buf, false, 0, 0, 0, fixed, Long.MAX_VALUE, topLevel, topLevel, topLevel.preferSplit);
    }

    /**
     * Re-initialize this block and return its old buffer.
     */
    public byte[] reinit(byte[] buf, boolean bigEndian, int start, int pos, int lim, byte sharing,
                         long totalLimit, long totalFlushed, boolean truncate, BufferedOutput rootBlock,
                         BufferedOutput parent, TopLevelBufferedOutput topLevel, boolean preferSplit) {
        byte[] b = this.buffer;
        this.buffer = buf;
        this.isBigEnd = bigEndian;
        this.start = start;
        this.position = pos;
        this.limit = lim;
        this.sharing = sharing;
        this.totalLimit = totalLimit;
        this.totalFlushed = totalFlushed;
        this.truncate = truncate;
        this.rootBlock = rootBlock;
        this.parent = parent;
        this.state = STATE_OPEN;
        this.topLevel = topLevel;
        this.preferSplit = preferSplit;
        return b;
    }

    protected void bufferClosed(boolean closeUpstream) throws IOException {
        if (parent != null) parent.appendNested(this);
    }

    public boolean isNocache() {
        return nocache;
    }

    public void setNocache(boolean nocache) {
        this.nocache = nocache;
    }

    public BufferedOutput getParent() {
        return parent;
    }

    public void setParent(BufferedOutput parent) {
        this.parent = parent;
    }
}
