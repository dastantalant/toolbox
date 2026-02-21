package com.toolbox.io.text.output;

import com.toolbox.io.buffer.BufferedOutput;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ASCIITextOutput extends ASCIICompatibleTextOutput {
    public ASCIITextOutput(BufferedOutput out, byte[] eol) {
        super(out, StandardCharsets.US_ASCII, eol);
    }

    @Override
    protected  void printRaw(String s) throws IOException {
        int len = s.length();
        if (len > 0) {
            int p = getOutput().fwd(len);
            byte[] buf = getOutput().getBuffer();

            for (int i = 0; i < len; i++) {
                char c = s.charAt(i);
                buf[p + i] = (c < 128) ? (byte) c : (byte) '?';
            }
        }
    }

    @Override
    protected   void printlnRaw(String s) throws IOException {
        int len = s.length();
        int p = getOutput().fwd(len + eolLen);
        byte[] buf = getOutput().getBuffer();

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            buf[p + i] = (c < 128) ? (byte) c : (byte) '?';
        }
        unsafeWriteEOL(p + len);
    }
}

