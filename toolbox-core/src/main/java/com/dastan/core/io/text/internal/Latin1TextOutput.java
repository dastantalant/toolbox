package com.dastan.core.io.text.internal;

import com.dastan.core.io.buffer.BufferedOutput;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Latin1TextOutput extends ASCIICompatibleTextOutput {
    public Latin1TextOutput(BufferedOutput out, byte[] eol) {
        super(out, StandardCharsets.ISO_8859_1, eol);
    }

    @Override
    protected  void printRaw(String s) throws IOException {
        int len = s.length();
        if (len > 0) {
            int p = getOutput().fwd(len);
            for (int i = 0; i < len; i++) {
                getOutput().setBuffer(p + i, (byte) s.charAt(i));
            }
        }
    }

    @Override
    protected  void printlnRaw(String s) throws IOException {
        int len = s.length();
        int p = getOutput().fwd(len + eolLen);
        for (int i = 0; i < len; i++) {
            getOutput().setBuffer(p + i, (byte) s.charAt(i));
        }
        unsafeWriteEOL(p + len);
    }
}
