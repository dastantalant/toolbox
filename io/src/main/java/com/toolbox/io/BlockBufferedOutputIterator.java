package com.toolbox.io;


import java.io.IOException;

final class BlockBufferedOutputIterator extends BufferIterator {
    private BufferedOutput block;
    private final BlockBufferedOutput root;

    BlockBufferedOutputIterator(BlockBufferedOutput root) { this.root = root; }

    private BufferedOutput skipEmpty(BufferedOutput b) {
        while(true) {
            if(b.getPosition() - b.getStart() > 0) return b;
            if(b == root) return null;
            b = b.getNext();
        }
    }

    public Object next() throws IOException {
        if(block == root) return null;
        BufferedOutput b = skipEmpty(block == null ? root.getNext() : block.getNext());
        if(b == null) return null;
        block = b;
        return b;
    }

    public void returnBuffer(Object id) {}

    public byte[] buffer() { return block.getBuffer(); }
    public int start() { return block.getStart(); }
    public int end() { return block.getPosition(); }
};

