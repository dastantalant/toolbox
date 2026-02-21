package com.toolbox.io;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/// A [BufferedOutput] which makes its data available for concurrent reading from another thread
/// via [#toBufferedInput()]  or [#toInputStream()].
public final class PipeBufferedOutput extends BlockFlushingBufferedOutput {
  private final AtomicBoolean connected = new AtomicBoolean(false);
  private final BlockingQueue<BufferedOutput> queue;
  private final BlockingQueue<BufferedOutput> returnQueue = new LinkedBlockingQueue<>();

  public PipeBufferedOutput(boolean bigEndian, int initialBufferSize, int blockDepth) {
    super(bigEndian, initialBufferSize);
    queue = new ArrayBlockingQueue<>(blockDepth);
  }

  @Override
  public PipeBufferedOutput order(ByteOrder order) {
    super.order(order);
    return this;
  }

  void put(BufferedOutput b) throws IOException {
    BufferedOutput r;
    while((r = returnQueue.poll()) != null)
      if(r != this) returnToCache((NestedBufferedOutput)r);
    try { queue.put(b); }
    catch(InterruptedException ex) { throw new IOException("Pipe transfer interrupted", ex); }
  }

  @Override
  protected void bufferClosed(boolean closeUpstream) throws IOException {
    super.bufferClosed(closeUpstream);
    //TODO can we support closeUpstream=false and connect a new BufferedOutput to the pipe?
    put(QueuedBufferIterator.END_MARKER);
  }

  /// Create a new [InputStream] that reads the data as it is written. This method is
  /// thread-safe. Only one call to [#toInputStream()] or [#toBufferedInput()] is allowed.
  public InputStream toInputStream() throws IOException {
    return new BufferIteratorInputStream(bufferIterator());
  }

  /// Create a new [BufferedInput] that reads the data as it is written. This method is
  /// thread-safe. Only one call to [#toInputStream()] or [#toBufferedInput()] is allowed.
  public BufferedInput toBufferedInput() throws IOException {
    return new SwitchingHeapBufferedInput(bufferIterator(), isBigEnd);
  }

  BufferIterator bufferIterator() throws IOException {
    if(!connected.compareAndSet(false, true)) throw new IOException("Pipe is already connected");
    return new QueuedBufferIterator(queue, returnQueue);
  }
}


