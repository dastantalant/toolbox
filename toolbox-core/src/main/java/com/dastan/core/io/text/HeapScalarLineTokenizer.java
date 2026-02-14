package com.dastan.core.io.text;

import com.dastan.core.io.buffer.BufferedInput;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract non-sealed class HeapScalarLineTokenizer extends HeapLineTokenizer {
    public static HeapScalarLineTokenizer of(BufferedInput in, Charset charset, byte eol, byte preEol) throws IOException {
        if(charset == StandardCharsets.ISO_8859_1) return new HeapScalarLineTokenizer(in, eol, preEol) {
            String makeString(byte[] buf, int start, int len) { return new String(buf, 0, start, len); }
        };
        else return new HeapScalarLineTokenizer(in, eol, preEol) {
            String makeString(byte[] buf, int start, int len) { return new String(buf, start, len, charset); }
        };
    }

    HeapScalarLineTokenizer(BufferedInput parentBin, byte eolChar, byte preEolChar) throws IOException {
        super(parentBin, eolChar, preEolChar);
    }

    private String rest(int p) {
        if(getBufferedInput().getPosition() < p) {
            var s = makeString(getBufferedInput().getBuffer(), getBufferedInput().getPosition(), p- getBufferedInput().getPosition());
            getBufferedInput().setPosition(p);
            return s;
        } else return null;
    }

    public final String readLine() throws IOException {
        var eol = eolChar;
        var bp = getBufferedInput().getPosition();
        var p = getBufferedInput().getPosition();
        while(true) {
            while(p < getBufferedInput().getLimit()) {
                var b = getBufferedInput().getBuffer(p);
                p += 1;
                if(b == eol) {
                    getBufferedInput().setPosition(p);
                    return emit(bp, p-1);
                }
            }
            var oldav = getBufferedInput().available();
            getBufferedInput().prepareAndFillBuffer((p- getBufferedInput().getPosition())+1);
            p -= bp - getBufferedInput().getPosition();
            if(oldav == getBufferedInput().available()) {
                return rest(p);
            }
            bp = getBufferedInput().getPosition();
        }
    }
}

