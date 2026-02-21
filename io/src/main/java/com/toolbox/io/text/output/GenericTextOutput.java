package com.toolbox.io.text.output;


import com.toolbox.io.buffer.BufferedOutput;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/// Generic TextOutput implementation that should work for arbitrary Charsets (including support for BOMs and end markers).
public class GenericTextOutput extends TextOutput {
    private final String eol;
    private final CharsetEncoder enc;

    public GenericTextOutput(BufferedOutput out, Charset cs, String eol) {
        super(out, cs);
        this.eol = eol;
        this.enc = cs.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    private byte[] prevBuf = null;
    private ByteBuffer bb = null;
    private final CharBuffer singleCharBuf = CharBuffer.allocate(1);

    private void updateBB(int p) {
        if (prevBuf != getOutput().getBuffer()) {
            bb = ByteBuffer.wrap(getOutput().getBuffer());
            prevBuf = getOutput().getBuffer();
        }
        bb.limit(getOutput().getPosition()).position(p);
    }

    private void printRawInner(CharBuffer cb, boolean endOfInput) throws IOException {
        int max = (int) (enc.maxBytesPerChar() * cb.length());
        int p = getOutput().tryFwd(max);
        updateBB(p);
        bb.limit(getOutput().getPosition()).position(p);
        CoderResult res = enc.encode(cb, bb, endOfInput);
        getOutput().setPosition(bb.position());
        if (res.isOverflow()) throw new EOFException();
        if (cb.remaining() == 1) {
            hasSurrogate = true;
            // surrogateCharBuf.put(0, cb.get()); // java 9
            surrogateCharBuf.clear();
            surrogateCharBuf.put(cb.get());
            surrogateCharBuf.flip();
        }
    }

    private void printRaw(CharBuffer cb) throws IOException {
        if (cb.remaining() > 0) {
            if (hasSurrogate) {
                hasSurrogate = false;
                if (cb.remaining() > 0) {
                    // surrogateCharBuf.put(1, cb.get()).position(0); // java 9
                    char first = surrogateCharBuf.get(0);
                    surrogateCharBuf.clear();
                    surrogateCharBuf.put(first);
                    surrogateCharBuf.put(cb.get());
                    surrogateCharBuf.flip();
                    printRawInner(surrogateCharBuf, false);
                }
            }
            printRawInner(cb, false);
        }
    }

    protected void printRaw(String s) throws IOException {
        printRaw(CharBuffer.wrap(s));
    }

    protected void printRawNull() throws IOException {
        printRaw("null");
    }

    protected void printlnRawNull() throws IOException {
        printlnRaw("null");
    }

    @Override
    public void close() throws IOException {
        if (hasSurrogate) {
            surrogateCharBuf.position(0).limit(1);
            printRawInner(surrogateCharBuf, true);
        } else {
            singleCharBuf.position(1);
            printRawInner(singleCharBuf, true);
        }
        bb.limit(getOutput().getLimit()).position(getOutput().getPosition());
        if (enc.flush(bb).isOverflow() && getOutput().getPosition() + 64 > getOutput().getLimit()) {
            int p = getOutput().tryFwd(64);
            updateBB(p);
            if (enc.flush(bb).isOverflow()) throw new EOFException();
            getOutput().setPosition(bb.position());
        }
        super.close();
    }

    protected void printlnRaw(String s) throws IOException {
        printRaw(s);
        printlnRaw();
    }

    protected void printlnRaw() throws IOException {
        printRaw(eol);
    }

    @Override
    public TextOutput print(char c) throws IOException {
        // singleCharBuf.position(0).put(0, c); // java 9
        singleCharBuf.clear();
        singleCharBuf.put(c);
        singleCharBuf.flip();
        printRaw(singleCharBuf);
        return this;
    }

    @Override
    public TextOutput println(char c) throws IOException {
        // singleCharBuf.position(0).put(0, c); // java 9
        singleCharBuf.clear();
        singleCharBuf.put(c);
        singleCharBuf.flip();
        printRaw(singleCharBuf);
        printlnRaw();
        maybeFlush();
        return this;
    }

    @Override
    public TextOutput print(char[] s) throws IOException {
        printRaw(CharBuffer.wrap(s));
        return this;
    }

    @Override
    public TextOutput println(char[] s) throws IOException {
        printRaw(CharBuffer.wrap(s));
        printlnRaw();
        maybeFlush();
        return this;
    }
}
