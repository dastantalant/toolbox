package com.toolbox.io.compression;

import com.toolbox.io.buffer.BufferedOutput;
import com.toolbox.io.buffer.WritableBuffer;
import com.toolbox.io.spi.FilterTask;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.zip.Deflater;

final class ParallelGzipBufferedOutput extends GzipBufferedOutput {

    ParallelGzipBufferedOutput(BufferedOutput parent, int depth, int minPartitionSize,
                               int maxPartitionSize, boolean batchSubmit, Executor pool, int compression) throws IOException {
        super(parent, false, depth, minPartitionSize, maxPartitionSize, batchSubmit, pool, compression);
    }

    @Override
    protected void finish() throws IOException {
        parent.int16l((short) 3);
        writeTrailer(parent, crc.getValue(), (int) totalBytesWritten());
    }

    @Override
    protected void filterBlock(BufferedOutput b) throws IOException {
        // Ideally we'd compute CRCs of each block in parallel and then merge them, but it's very
        // fast compared to the deflate compression and Java doesn't expose the existing
        // `crc32_combine()` from zlib for our use.
        crc.update(b.getBuffer(), b.getStart(), b.getPosition() - b.getStart());
        super.filterBlock(b);
    }

    protected void filterAsync(FilterTask<Deflater> task) {
        Deflater defl = task.getData() != null ? task.getData() : makeDeflater();
        task.setData(defl);
        try {
            WritableBuffer o = task.getWritableBuffer();
            if (!task.isEmpty()) {
                if (task.isNew()) defl.setInput(task.getBuffer(), task.getStart(), task.length());
                o.setPosition(o.getPosition() + defl.deflate(o.getBuffer(), o.getPosition(), o.getBuffer().length - o.getPosition()));
            }
            if (defl.needsInput()) {
                if (task.isLast()) {
                    int l;
                    while ((l = defl.deflate(o.getBuffer(), o.getPosition(), o.getBuffer().length - o.getPosition(), Deflater.SYNC_FLUSH)) > 0)
                        o.setPosition(o.getPosition() + l);
                    if (o.getPosition() < o.getBuffer().length) {
                        defl.end();
                        task.consume();
                    }
                } else task.consume();
            }
        } catch (Error | RuntimeException ex) {
            defl.end();
            throw ex;
        }
    }
}
