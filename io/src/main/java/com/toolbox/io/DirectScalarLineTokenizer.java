package com.toolbox.io;

import java.io.IOException;
import java.lang.foreign.ValueLayout;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class DirectScalarLineTokenizer extends DirectLineTokenizer {
    public static DirectScalarLineTokenizer of(BufferedInput in, Charset charset, byte eol, byte preEol) throws IOException {
        if (charset == StandardCharsets.ISO_8859_1) {
            return new DirectScalarLineTokenizer(in, eol, preEol) {
                @Override
                String makeString(byte[] buf, int start, int len) {
                    return new String(buf, start, len, StandardCharsets.ISO_8859_1);
                }
            };
        } else {
            return new DirectScalarLineTokenizer(in, eol, preEol) {
                @Override
                String makeString(byte[] buf, int start, int len) {
                    return new String(buf, start, len, charset);
                }
            };
        }
    }

    private long pos;
    private final long limit;

    DirectScalarLineTokenizer(BufferedInput bin, byte eolChar, byte preEolChar) throws IOException {
        super(bin, eolChar, preEolChar);
        this.pos = getStart();
        limit = bin.getTotalReadLimit();
        //System.out.println("  Created "+show());
    }

    String show() {
        return "start=" + getStart() + ", pos=" + pos + ", limit=" + limit;
    }

    @Override
    public void markClosed() {
        super.markClosed();
        pos = limit;
    }

    private String rest() throws IOException {
        checkState();
        if (getStart() < pos) {
            String s = makeString(getMemorySegment(), getStart(), pos - getStart());
            setStart(pos);
            return s;
        } else return null;
    }

    public final String readLine() throws IOException {
        long p = pos;
        while (p < limit) {
            byte b = getMemorySegment().get(ValueLayout.JAVA_BYTE, p);
            p += 1;
            if (b == eolChar) {
                String s = emit(getStart(), p - 1);
                setStart(p);
                pos = p;
                return s;
            }
        }
        pos = p;
        return rest();
    }
}
