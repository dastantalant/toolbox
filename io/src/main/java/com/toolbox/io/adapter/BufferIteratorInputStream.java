package com.toolbox.io.adapter;

import com.toolbox.io.util.IndexCheckUtil;

import java.io.IOException;
import java.io.InputStream;

public final class BufferIteratorInputStream extends InputStream {
    private final BufferIterator iterator;
    private byte[] buffer;
    private Object bufferId;
    private int position;
    private int limit;

    public BufferIteratorInputStream(BufferIterator iterator) { this.iterator = iterator; }

    // Make data available in the current buffer, advancing it if necessary. Returns
    // false if the end of the data has been reached.
    private boolean request() throws IOException {
        if(limit - position <= 0) {
            if(bufferId != null) iterator.returnBuffer(bufferId);
            if((bufferId = iterator.next()) == null) return false;
            buffer = iterator.buffer();
            position = iterator.start();
            limit = iterator.end();
        }
        return true;
    }

    public int read() throws IOException {
        if(!request()) return -1;
        return buffer[position++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        IndexCheckUtil.checkFromIndexSize(off, len, b.length);
        if(len == 0) return 0;
        if(!request()) return -1;
        int r = Math.min(len, available());
        System.arraycopy(buffer, position, b, off, r);
        position += r;
        return r;
    }

    @Override
    public long skip(long n) throws IOException {
        long rem = n;
        while(true) {
            if(rem <= available()) {
                position += (int)rem;
                return n;
            } else {
                rem -= available();
                if(!request()) return n-rem;
            }
        }
    }

    @Override
    public int available() { return limit - position; }
}
