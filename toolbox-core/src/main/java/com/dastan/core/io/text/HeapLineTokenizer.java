package com.dastan.core.io.text;

import com.dastan.core.io.CloseableView;
import com.dastan.core.io.buffer.BufferedInput;

import java.io.IOException;

public abstract sealed class HeapLineTokenizer extends LineTokenizer implements CloseableView permits HeapScalarLineTokenizer, HeapVectorizedLineTokenizer {
  private final BufferedInput parentBufferedInput;
  private final BufferedInput bufferedInput;

  HeapLineTokenizer(BufferedInput parentBufferedInput, byte eolChar, byte preEolChar) throws IOException {
    super(eolChar, preEolChar);
    this.parentBufferedInput = parentBufferedInput;
    this.bufferedInput = parentBufferedInput.identicalView(this);
  }

  abstract String makeString(byte[] buf, int start, int len);

  @Override
  public void markClosed() {
    super.markClosed();
  }

  public LineTokenizer detach() throws IOException {
    bufferedInput.detach();
    return this;
  }

  String emit(int start, int lfpos) {
    var end = lfpos > 0 && preEolChar != (byte)(-1) && bufferedInput.getBuffer(lfpos-1) == preEolChar ? lfpos-1 : lfpos;
    return start == end ? "" : makeString(bufferedInput.getBuffer(), start, end-start);
  }

  public void close(boolean closeUpstream) throws IOException {
    if(!closed) {
      if(closeUpstream) parentBufferedInput.close();
      else bufferedInput.close();
      markClosed();
    }
  }

  public BufferedInput getParentBufferedInput() {
    return parentBufferedInput;
  }

  public BufferedInput getBufferedInput() {
    return bufferedInput;
  }
}

