package com.toolbox.core.io.buffer.internal;

import com.toolbox.core.io.adapter.BufferIterator;
import com.toolbox.core.io.adapter.BufferIteratorInputStream;
import com.toolbox.core.io.buffer.AccumulatingBufferedOutput;
import com.toolbox.core.io.buffer.BufferedInput;
import com.toolbox.core.io.buffer.BufferedOutput;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/// A [BufferedOutput] which collects data in one or more byte array segments. The data can be
/// accessed with [#toBufferedInput()] or [#toInputStream()] after closing the buffer.
/// 
/// Instances are created with [BufferedOutput#ofBlocks(int)] (and its overloads).
public final class BlockBufferedOutput extends AccumulatingBufferedOutput {

  public BlockBufferedOutput(boolean bigEndian, int initialBufferSize) {
    super(new byte[initialBufferSize], bigEndian, 0, 0, initialBufferSize, initialBufferSize, false, Long.MAX_VALUE, true);
  }

  @Override
  public BlockBufferedOutput order(ByteOrder order) {
    super.order(order);
    return this;
  }

  public void flushBlocks(boolean forceFlush) throws IOException {}

  public InputStream toInputStream() throws IOException {
    return new BufferIteratorInputStream(bufferIterator());
  }

  public BufferedInput toBufferedInput() throws IOException {
    return new SwitchingHeapBufferedInput(bufferIterator(), isBigEnd);
  }

  BufferIterator bufferIterator() throws IOException {
    if(state != BufferedOutput.STATE_CLOSED) throw new IOException("Cannot access buffer before closing");
    return new BlockBufferedOutputIterator(this);
  }
}


