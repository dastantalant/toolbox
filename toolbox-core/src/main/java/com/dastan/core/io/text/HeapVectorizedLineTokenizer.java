package com.dastan.core.io.text;


import com.dastan.core.io.buffer.BufferedInput;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.dastan.core.io.internal.VectorSupport.FULL_MASK;
import static com.dastan.core.io.internal.VectorSupport.SPECIES;
import static com.dastan.core.io.internal.VectorSupport.LEN;

/// A vectorized implementation of [[HeapLineTokenizer]].
///
/// The parser uses explicit SIMD loops with up to 512 bits / 64 lanes per vector (depending on
/// hardware and JVM support).
public abstract non-sealed class HeapVectorizedLineTokenizer extends HeapLineTokenizer {
    public static HeapVectorizedLineTokenizer of(BufferedInput in, Charset charset, byte eol, byte preEol) throws IOException {
        if(charset == StandardCharsets.ISO_8859_1) return new HeapVectorizedLineTokenizer(in, eol, preEol) {
            String makeString(byte[] buf, int start, int len) { return new String(buf, 0, start, len); }
        };
        else return new HeapVectorizedLineTokenizer(in, eol, preEol) {
            String makeString(byte[] buf, int start, int len) { return new String(buf, start, len, charset); }
        };
    }

    private int vpos; // start of the current vector in buf
    private long mask = 0L; // vector mask that marks the LFs
    private final ByteVector eolVector;

    HeapVectorizedLineTokenizer(BufferedInput parentBin, byte eolChar, byte preEolChar) throws IOException {
        super(parentBin, eolChar, preEolChar);
        this.vpos = getBufferedInput().getPosition() - LEN;
        this.mask = 0L;
        this.eolVector = ByteVector.broadcast(SPECIES, eolChar);
        if(getBufferedInput().getPosition() < getBufferedInput().getLimit()) {
            // Make sure the position is aligned if we have buffered data.
            // Otherwise we leave the initial buffering to the main loop in readLine().
            vpos += LEN;
            alignPosAndComputeMask();
        }
    }

    private int buffer() throws IOException {
        if(vpos == Integer.MAX_VALUE) {
            vpos = Integer.MAX_VALUE - LEN;
            return 0;
        }
        var scanned = getBufferedInput().getLimit() - getBufferedInput().getPosition();
        var oldav = getBufferedInput().available();
        getBufferedInput().prepareAndFillBuffer(oldav + LEN);
        if(oldav == getBufferedInput().available()) return 0;
        vpos = getBufferedInput().getPosition() + scanned;
        alignPosAndComputeMask();
        return 1;
    }

    @Override
    public void markClosed() {
        super.markClosed();
        mask = 0L;
        vpos = Integer.MAX_VALUE - LEN;
    }

    private void alignPosAndComputeMask() {
        var offsetInVector = vpos % LEN;
        vpos -= offsetInVector;
        mask = computeMask() & (-1L >>> offsetInVector << offsetInVector);
    }

    private String rest() throws IOException {
        checkState();
        String r;
        if(vpos != Integer.MAX_VALUE) {
            var l = getBufferedInput().available();
            mask = 0;
            if(l != 0) {
                var s = makeString(getBufferedInput().getBuffer(), getBufferedInput().getPosition(), l);
                getBufferedInput().setPosition(getBufferedInput().getLimit());
                r = s;
            } else r = null;
        } else r = null;
        vpos = Integer.MAX_VALUE - LEN;
        return r;
    }

    public final String readLine() throws IOException {
        while(true) {
            var f = Long.numberOfTrailingZeros(mask);
            if(f < LEN) {
                var lfpos = vpos+f;
                var s = emit(getBufferedInput().getPosition(), lfpos);
                getBufferedInput().setPosition(lfpos + 1);
                mask &= ~(1L<<f);
                return s;
            }
            vpos += LEN;
            if(vpos <= getBufferedInput().getLimit() - LEN) mask = computeSimpleMask();
            else if(vpos < getBufferedInput().getLimit()) mask = computeMask();
            else if(buffer() == 0) return rest();
        }
    }
    private long computeSimpleMask() {
        return ByteVector.fromArray(SPECIES, getBufferedInput().getBuffer(), vpos).compare(VectorOperators.EQ, eolVector).toLong();
    }

    private long computeMask() {
        var excess = vpos+ LEN - getBufferedInput().getLimit();
        var v = excess <= 0
                ? ByteVector.fromArray(SPECIES, getBufferedInput().getBuffer(), vpos)
                : ByteVector.fromArray(SPECIES, getBufferedInput().getBuffer(), vpos, VectorMask.fromLong(SPECIES, FULL_MASK >>> excess));
        // Seems to be slightly faster than comparing against a scalar:
        return v.compare(VectorOperators.EQ, eolVector).toLong();
    }
}
