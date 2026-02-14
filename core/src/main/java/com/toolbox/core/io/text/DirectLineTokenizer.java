package com.toolbox.core.io.text;

import com.toolbox.core.io.CloseableView;
import com.toolbox.core.io.buffer.BufferedInput;
import com.toolbox.core.io.internal.LineBuffer;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;


public abstract sealed class DirectLineTokenizer extends LineTokenizer implements CloseableView permits DirectScalarLineTokenizer, DirectVectorizedLineTokenizer {
  private final BufferedInput bufferedInput;
  private final MemorySegment memorySegment;
  private long start;
  private final int initialPosition;
  private final LineBuffer lineBuffer = new LineBuffer();

  DirectLineTokenizer(BufferedInput bufferedInput, byte eolChar, byte preEolChar) throws IOException {
    super(eolChar, preEolChar);
    this.bufferedInput = bufferedInput;
    this.memorySegment = bufferedInput.getMemorySegment();
    this.start = bufferedInput.getBbStart() + bufferedInput.getPosition();
    this.initialPosition = bufferedInput.getPosition();
    bufferedInput.lock(this);
  }


  @Override
  public void markClosed() {
    super.markClosed();
  }

  public LineTokenizer detach() { return this; }

  abstract String makeString(byte[] buf, int start, int len);

  String makeString(MemorySegment buf, long start, long llen) {
    return makeStringGeneric(buf, start, llen);
  }

  final String makeStringGeneric(MemorySegment buf, long start, long llen) {
    var len = (int)llen;
    var b = lineBuffer.get(len);
    MemorySegment.copy(buf, ValueLayout.JAVA_BYTE, start, b, 0, len);
    return makeString(b, 0, len);
  }

  String emit(long start, long lfpos) {
    var end = lfpos > 0 && preEolChar != (byte)(-1) && memorySegment.get(ValueLayout.JAVA_BYTE, lfpos-1) == preEolChar ? lfpos-1 : lfpos;
    return start == end ? "" : makeString(memorySegment, start, end-start);
  }

  public void close(boolean closeUpstream) throws IOException {
    if(!closed) {
      bufferedInput.unlock();
      bufferedInput.setPosition(initialPosition);
      if(closeUpstream) {
        bufferedInput.close();
      } else {
        var origStart = bufferedInput.getBbStart() + bufferedInput.getPosition();
        bufferedInput.skip(start-origStart);
      }
      markClosed();
    }
  }

  public BufferedInput getBufferedInput() {
    return bufferedInput;
  }

  public MemorySegment getMemorySegment() {
    return memorySegment;
  }

  public long getStart() {
    return start;
  }

  public void setStart(long start) {
    this.start = start;
  }

  public int getInitialPosition() {
    return initialPosition;
  }

  public LineBuffer getLineBuffer() {
    return lineBuffer;
  }
}



