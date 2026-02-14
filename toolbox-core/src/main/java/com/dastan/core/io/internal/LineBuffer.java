package com.dastan.core.io.internal;

public class LineBuffer {
  private byte[] buffer = new byte[256];

  private byte[] extend(int len) {
    var buflen = buffer.length;
    while(buflen < len) buflen *= 2;
    return new byte[buflen];
  }

  public byte[] get(int len) {
    if(buffer.length < len) buffer = extend(len);
    return buffer;
  }
}
