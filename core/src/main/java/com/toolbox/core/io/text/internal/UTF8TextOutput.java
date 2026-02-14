package com.toolbox.core.io.text.internal;

import com.toolbox.core.io.buffer.BufferedOutput;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UTF8TextOutput extends ASCIICompatibleTextOutput {
    public UTF8TextOutput(BufferedOutput out, byte[] eol) {
        super(out, StandardCharsets.UTF_8, eol);
    }

    @Override
    protected  void printRaw(String s) throws IOException {
        if (!s.isEmpty()) {
            byte[] b = s.getBytes(StandardCharsets.UTF_8);
            getOutput().append(b);
        }
    }

    @Override
    protected  void printlnRaw(String s) throws IOException {
        if (!s.isEmpty()) {
            byte[] b = s.getBytes(StandardCharsets.UTF_8);
            int l = b.length;
            int p = getOutput().fwd(l + eolLen);
            System.arraycopy(b, 0, getOutput().getBuffer(), p, l);
            unsafeWriteEOL(p + l);
        } else {
            printlnRaw();
        }
    }
}
