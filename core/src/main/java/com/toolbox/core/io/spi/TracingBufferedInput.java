package com.toolbox.core.io.spi;

import com.toolbox.core.io.buffer.BufferedInput;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Checksum;

/// A [BufferedInput] that maintains a trace of the read data.
/// 
/// Note that unlike [java.util.zip.CheckedInputStream] this class buffers the input, and the trace
/// is usually trailing behind the current position. If you need an up-to-date trace before
/// closing the stream, you have to call [#updateTrace()] first.
public abstract class TracingBufferedInput extends BufferedInput {

  /// Create a [TracingBufferedInput] that updates a [Checksum], similar to
  /// [java.util.zip.CheckedInputStream].
  /// 
  /// @param checksum The Checksum to update
  public static TracingBufferedInput checked(BufferedInput parent, Checksum checksum) {
    return new TracingBufferedInput(parent) {
      @Override
      protected void trace(byte[] buf, int off, int len) { checksum.update(buf, off, len); }
      protected void trace(ByteBuffer bb) { checksum.update(bb); }
    };
  }
  
  private final BufferedInput parent;
  private long traced = 0;
  private final TracingBufferedInput root;

  protected TracingBufferedInput(BufferedInput parent) {
    super(parent.getBuffer(), parent.getPosition(), parent.getLimit(), Long.MAX_VALUE, null, parent.isBigEnd(), parent.getLineBuffer(), parent.getMemorySegment(), parent.getByteBuffer(), parent);
    this.parent = parent;
    this.root = this;
  }

  private TracingBufferedInput(TracingBufferedInput viewParent) {
    super(null, 0, 0, 0, viewParent, viewParent.isBigEnd, viewParent.lineBuffer, viewParent.memorySegment, viewParent.byteBuffer, null);
    this.parent = viewParent.parent;
    this.root = viewParent.root;
  }

  /// This method is called to update the trace to include the specified byte array segment
  /// when reading from a `byte[]`-based BufferedInput. The default implementation wraps the
  /// byte array in a [ByteBuffer] and calls [#trace(ByteBuffer)]. It can be overridden to handle
  /// this case more efficiently.
  protected void trace(byte[] buf, int off, int len) {
    trace(ByteBuffer.wrap(buf, off, len));
  }

  /// This method is called to uodate the trace to include the specified ByteBuffer's contents.
  protected abstract void trace(ByteBuffer bb);

  @Override
  protected BufferedInput createEmptyView() {
    return new TracingBufferedInput(this) {
      protected void trace(ByteBuffer bb) {}
    };
  }

  @Override
  protected void copyBufferFrom(BufferedInput b) {
    super.copyBufferFrom(b);
    var bb = (TracingBufferedInput)b;
    traced = bb.traced;
  }

  protected void bufferClosed(boolean closeUpstream) throws IOException {
    update();
    parent.setPosition(position);
  }

  private void update() {
    //System.out.println("update() "+show());
    var tbuf = totalBuffered + parentTotalOffset;
    if(tbuf > traced) {
      var bstart = limit - (int)(tbuf - traced);
      var bend = limit;
      traced = tbuf;
      if(buffer != null) root.trace(buffer, bstart, bend-bstart);
      else root.trace(byteBuffer.slice(bstart, bend-bstart));
    }
  }

  @Override
  public void prepareAndFillBuffer(int count) throws IOException {
    update();
    //System.out.println("prepareAndFillBuffer("+count+") "+show());
    if(totalBuffered < totalReadLimit) {
      var rem0 = limit - position;
      parent.setPosition(position);
      parent.prepareAndFillBuffer(count);
      position = parent.getPosition();
      limit = parent.getLimit();
      buffer = parent.getBuffer();
      byteBuffer = parent.getByteBuffer();
      var rem1 = limit - position;
      totalBuffered += (rem1-rem0);
      clampToLimit();
    }
  }

  /// Ensure that the trace reflects all data that has been read from this
  /// TracingBufferedInput. This method is safe to call after closing, in which case
  /// it does nothing.
  public void updateTrace() throws IOException {
    if(!isClosed()) checkState();
    update();
  }
}
