package com.toolbox.io;

import java.io.IOException;
import java.nio.charset.Charset;

/// Base class for filtering [BufferedInput] implementations that read from another [BufferedInput]
/// and process the data.
///
/// Implementations are currently limited to producing heap-based buffers, but they can read from
/// heap-based or direct buffers.
public abstract class FilteringBufferedInput extends SwitchingHeapBufferedInput {
    private final BufferedInput parent;
    protected final BufferedInput parentView;
    protected final FilteringBufferedInput rootFilter;
    private final int blockSize;
    private OutputBlock cache;

    /// Constructor to be called by subclasses.
    ///
    /// @param parent    The parent buffer to read from.
    /// @param blockSize The block size of this filter, if it is the root filter. When multiple
    ///                                     filters are stacked, the lowest one contains the shared cache and determines the block
    ///                                     size. Consequently, this argument is ignored if the parent is already a
    ///                                     [FilteringBufferedInput] or a view of one.
    protected FilteringBufferedInput(BufferedInput parent, int blockSize) throws IOException {
        //TODO auto buffer size from parent
        super(new InternalIterator(), parent.isBigEnd());
        this.parent = parent;
        parentView = parent.identicalView((InternalIterator) iterator);
        if (parent.getViewRoot() instanceof FilteringBufferedInput) {
            FilteringBufferedInput input = (FilteringBufferedInput) parent.getViewRoot();
            this.rootFilter = input.rootFilter;
            this.blockSize = input.rootFilter.blockSize;
        } else {
            this.rootFilter = this;
            this.blockSize = blockSize;
        }
        ((InternalIterator) iterator).outer = this;
    }

    protected void bufferClosed(boolean closeUpstream) throws IOException {
        parentView.close();
        cleanUp();
        if (closeUpstream) parent.close();
    }

    /// Clean up resources when the filter is closed.
    protected void cleanUp() throws IOException {
    }

    // Must only be called on the rootFilter
    private OutputBlock allocBlock() {
        OutputBlock b;
        if (cache == null) {
            b = new OutputBlock();
            b.setBuffer(new byte[blockSize]);
        } else {
            b = cache;
            cache = b.next;
            b.next = null;
            b.setPosition(0);
        }
        b.setLimit(b.getBuffer().length);
        b.setBigEnd(isBigEnd);
        return b;
    }

    // Must only be called on the rootFilter
    private void releaseBlock(OutputBlock b) {
        b.next = cache;
        cache = b;
    }

    /// Read data from `from` and produce at least 1 byte of filtered output in `to`.
    /// Producing an empty block indicates the end of the input.
    protected abstract void filterBlock(BufferedInput from, WritableBuffer<?> to) throws IOException;

    private static final class OutputBlock extends WritableBuffer<OutputBlock> {
        private OutputBlock next;

        protected void flushAndGrow(int count) {
        }

        public Output reserve(long length) throws IOException {
            return null;
        }

        public Output defer(long max) throws IOException {
            return null;
        }

        public Output defer() throws IOException {
            return null;
        }

        public TextOutput text(Charset charset, String lineSeparator) {
            return null;
        }

        public TextOutput text(Charset charset) {
            return null;
        }

        public TextOutput text() {
            return null;
        }

        public void close() throws IOException {
        }

        public void flush() throws IOException {
        }
    }

    private static class InternalIterator extends BufferIterator implements CloseableView {
        private FilteringBufferedInput outer;
        private OutputBlock to;

        public Object next() throws IOException {
            to = outer.rootFilter.allocBlock();
            outer.filterBlock(outer.parentView, to);
            return to.getStart() == to.getPosition() ? null : to;
        }

        public byte[] buffer() {
            return to.getBuffer();
        }

        public int start() {
            return to.getStart();
        }

        public int end() {
            return to.getPosition();
        }

        public void returnBuffer(Object id) {
            outer.rootFilter.releaseBlock((OutputBlock) id);
        }
        public void markClosed() {
            outer.markClosed();
        }
    }

}
