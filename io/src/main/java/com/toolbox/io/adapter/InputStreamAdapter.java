package com.toolbox.io.adapter;

import com.toolbox.io.buffer.BufferedInput;

import java.io.IOException;
import java.io.InputStream;

public final class InputStreamAdapter extends InputStream {
  private final BufferedInput bufferedInput;

  public InputStreamAdapter(BufferedInput bufferedInput) { this.bufferedInput = bufferedInput; }

  public int read() throws IOException { return bufferedInput.read(); }

  public int read(byte[] b, int off, int len) throws IOException { return bufferedInput.readSome(b, off, len); }

  public int readNBytes(byte[] b, int off, int len) throws IOException { return bufferedInput.read(b, off, len); }

  public long skip(long n) throws IOException { return bufferedInput.skipSome(n); }

  public void skipNBytes(long n) throws IOException { bufferedInput.skip(n); }

  public int available() throws IOException { return bufferedInput.available(); }

  public void close() throws IOException { bufferedInput.close(true); }
}
