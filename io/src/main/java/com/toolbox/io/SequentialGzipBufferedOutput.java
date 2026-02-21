package com.toolbox.io;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.zip.Deflater;

final class SequentialGzipBufferedOutput extends GzipBufferedOutput {
    private final Deflater deflater;
    private boolean isEnd;

    SequentialGzipBufferedOutput(BufferedOutput parent, int compression) throws IOException {
        super(parent, compression);
        this.deflater = makeDeflater();
    }

    SequentialGzipBufferedOutput(BufferedOutput parent, int depth, int minPartitionSize,
                                 int maxPartitionSize, boolean batchSubmit, Executor pool, int compression) throws IOException {
        super(parent, true, depth, minPartitionSize, maxPartitionSize, batchSubmit, pool, compression);
        this.deflater = makeDeflater();
    }

    @Override
    protected void finish() throws IOException {
        if (!isEnd && !deflater.finished()) {
            try {
                deflater.finish();
                while (!deflater.finished()) deflate();
                writeTrailer(parent, crc.getValue(), deflater.getTotalIn());
            } finally {
                isEnd = true;
                deflater.end();
            }
        }
    }

    @Override
    protected void flushPending() throws IOException {
        if (!flushPartial) super.flushPending();
    }

    private void deflate() throws IOException {
        parent.ensureAvailable(1);
        int l = deflater.deflate(parent.getBuffer(), parent.getPosition(), parent.getLimit() - parent.getPosition());
        parent.setPosition(parent.getPosition() + l);
    }

    @Override
    protected void filterBlock(BufferedOutput b) throws IOException {
        if (!flushPartial) super.filterBlock(b);
        else {
            deflater.setInput(b.getBuffer(), b.getStart(), b.getPosition() - b.getStart());
            while (!deflater.needsInput()) deflate();
            crc.update(b.getBuffer(), b.getStart(), b.getPosition() - b.getStart());
            if (b.getState() != STATE_OPEN) releaseBlock(b);
        }
    }

    protected void filterAsync(FilterTask<Deflater> t) {
        try {
            if (t.isNew()) {
                crc.update(t.getBuffer(), t.getStart(), t.getEnd() - t.getStart());
                deflater.setInput(t.getBuffer(), t.getStart(), t.getEnd() - t.getStart());
            }
            WritableBuffer o = t.getWritableBuffer();
            o.setPosition(o.getPosition() + deflater.deflate(o.getBuffer(), o.getStart(), o.getBuffer().length - o.getStart()));
            if (deflater.needsInput()) t.consume();
        } catch (Error | RuntimeException e) {
            isEnd = true;
            deflater.end();
        }
    }
}
