package com.toolbox.io;

import java.io.IOException;
import java.io.InputStream;

public final class StreamingHeapBufferedInput extends BufferedInput {
  final InputStream in;
  final int minRead;

  public StreamingHeapBufferedInput(byte[] buf, int pos, int lim, long totalReadLimit, InputStream in, int minRead, BufferedInput viewParent, boolean bigEndian) {
    super(buf, pos, lim, totalReadLimit, viewParent, bigEndian, null, null, null, in);
    this.in = in;
    this.minRead = minRead;
  }

  private int fillBuffer() throws IOException {
    int read = in.read(buffer, limit, buffer.length - limit);
    if(read > 0) {
      totalBuffered += read;
      limit += read;
      clampToLimit();
    }
    return read;
  }

  public void prepareAndFillBuffer(int count) throws IOException {
    checkState();
    if(in != null && totalBuffered < totalReadLimit) {
      if(position + count > buffer.length || position >= buffer.length - minRead)
        shiftOrGrowBuf(count);
      while(fillBuffer() >= 0 && available() < count) {}
    }
  }

  protected BufferedInput createEmptyView() {
    return new StreamingHeapBufferedInput(null, 0, 0, 0, in, minRead, this, isBigEnd);
  }

  @Override
  public int read(byte[] a, int off, int len) throws IOException {
    IndexCheckUtil.checkFromIndexSize(off, len, a.length);
    int copied = Math.min(len, available());
    if(copied > 0) {
      System.arraycopy(buffer, position, a, off, copied);
      position += copied;
    }
    int rem = len - copied;
    // Copy directly without buffering
    while(rem >= minRead && in != null) {
      int r = in.read(a, off + copied, rem);
      if(r <= 0) return copied;
      totalBuffered += r;
      copied += r;
      rem -= r;
    }
    if(rem > 0) {
      requestAvailable(rem);
      int av = available();
      if(av > 0) {
        rem = Math.min(rem, available());
        System.arraycopy(buffer, position, a, off + copied, rem);
        copied += rem;
      }
    }
    return copied;
  }

  @Override
  public long skip(long bytes) throws IOException {
    checkState();
    long base = 0L;
    while(true) {
      if(bytes <= 0) return base;
      int skipAv = (int)Math.min(bytes, available());
      position += skipAv;
      long rem = bytes - skipAv;
      if(rem <= 0 || in == null) return base + skipAv;
      long remTotal = totalReadLimit - totalBuffered;
      rem = Math.min(rem, remTotal);
      // Skip directly without buffering
      rem -= trySkipIn(rem); //TODO have a minSkip similar to minRead?
      if(rem <= 0) return base + bytes;
      requestAvailable((int)Math.min(rem, buffer.length));
      if(available() <= 0) return base + bytes - rem;
      base = base + bytes - rem;
      bytes = rem;
    }
  }

  // repeatedly try in.skip(); may return early even when more data is available
  private long trySkipIn(long b) throws IOException {
    long skipped = 0L;
    while(skipped < b) {
      long l = in.skip(b - skipped);
      if(l <= 0) return skipped;
      totalBuffered += l;
      skipped += l;
    }
    return skipped;
  }
}
