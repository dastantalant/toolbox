package com.toolbox.io.adapter;

import com.toolbox.io.adapter.BufferIterator;
import com.toolbox.io.buffer.BufferedOutput;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public final class QueuedBufferIterator extends BufferIterator {
    public static final BufferedOutput END_MARKER = BufferedOutput.growing(0);

    static { try { END_MARKER.close(); } catch (IOException ignored) {} }

    private final BlockingQueue<BufferedOutput> queue, returnQueue;
    private byte[] buffer;
    private int start, end;
    private boolean finished;

    public QueuedBufferIterator(BlockingQueue<BufferedOutput> queue, BlockingQueue<BufferedOutput> returnQueue) {
        this.queue = queue;
        this.returnQueue = returnQueue;
    }

    public byte[] buffer() { return buffer; }
    public int start() { return start; }
    public int end() { return end; }

    public Object next() throws IOException {
        if(finished) return null;
        BufferedOutput b;
        try { b = queue.take(); }
        catch(InterruptedException ex) { throw new IOException("Pipe transfer interrupted", ex); }
        if(b == END_MARKER) {
            finished = true;
            buffer = null;
            return null;
        }
        buffer = b.getBuffer();
        start = b.getStart();
        end = b.getPosition();
        return b;
    }

    public void returnBuffer(Object id) {
        if(!finished) returnQueue.offer((BufferedOutput)id);
    }
}

