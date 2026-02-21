package com.toolbox.io;

import java.io.IOException;
import java.io.OutputStream;

public final class OutputStreamAdapter extends OutputStream {
  private final BufferedOutput bufferedOutput;

  public OutputStreamAdapter(BufferedOutput bufferedOutput) { this.bufferedOutput = bufferedOutput; }

  public void write(int b) throws IOException { bufferedOutput.uint8(b); }

  @Override
  public void write(byte[] bytes, int off, int len) throws IOException { bufferedOutput.append(bytes, off, len); }

  @Override
  public void flush() throws IOException { bufferedOutput.flush(); }

  @Override
  public void close() throws IOException { bufferedOutput.close(true); }
}
