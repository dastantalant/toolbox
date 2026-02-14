package com.toolbox.core.io.text;


import com.toolbox.core.io.buffer.BufferedInput;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.toolbox.core.io.internal.VectorSupport.FULL_MASK;
import static com.toolbox.core.io.internal.VectorSupport.SPECIES;
import static com.toolbox.core.io.internal.VectorSupport.LEN;

/// A vectorized implementation of [[DirectLineTokenizer]].
///
/// The parser uses explicit SIMD loops with up to 512 bits / 64 lanes per vector (depending on
/// hardware and JVM support).
public abstract non-sealed class DirectVectorizedLineTokenizer extends DirectLineTokenizer {

    public static DirectVectorizedLineTokenizer of(BufferedInput in, Charset charset, byte eol, byte preEol) throws IOException {
        if (charset == StandardCharsets.ISO_8859_1) {
            return new DirectVectorizedLineTokenizer(in, eol, preEol) {
                @Override
                String makeString(byte[] buf, int start, int len) {
                    return new String(buf, start, len, StandardCharsets.ISO_8859_1);
                }
            };
        } else {
            return new DirectVectorizedLineTokenizer(in, eol, preEol) {
                @Override
                String makeString(byte[] buf, int start, int len) {
                    return new String(buf, start, len, charset);
                }
            };
        }
    }
    private long vpos;
    private final long limit;
    private long mask = 0L;
    private final ByteVector eolVector;

    DirectVectorizedLineTokenizer(BufferedInput bin, byte eolChar, byte preEolChar) throws IOException {
        super(bin, eolChar, preEolChar);
        this.vpos = getStart() - LEN;
        this.limit = getMemorySegment().byteSize();
        this.eolVector = ByteVector.broadcast(SPECIES, eolChar);
    }

    @Override
    public void markClosed() {
        super.markClosed();
        mask = 0L;
        vpos = Long.MAX_VALUE - LEN;
    }

    private String rest() throws IOException {
        String r;
        if(vpos != Long.MAX_VALUE) {
            var l = limit-getStart();
            mask = 0;
            if(l != 0) {
                var s = makeString(getMemorySegment(), getStart(), l);
                setStart(limit);
                r = s;
            } else r = null;
        } else {
            checkState();
            r = null;
        }
        vpos = Long.MAX_VALUE - LEN;
        return r;
    }

    public String readLine() throws IOException {
        while(true) {
            var f = Long.numberOfTrailingZeros(mask);
            if(f < LEN) {
                var lfpos = vpos+f;
                var s = emit(getStart(), lfpos);
                setStart(lfpos+1);
                mask &= ~(1L<<f);
                return s;
            }
            vpos += LEN;
            if(vpos <= limit- LEN) mask = computeSimpleMask();
            else if(vpos < limit) mask = computeMask();
            else return rest();
        }
    }

    private long computeSimpleMask() {
        return ByteVector.fromMemorySegment(SPECIES, getMemorySegment(), vpos, ByteOrder.BIG_ENDIAN).compare(VectorOperators.EQ, eolVector).toLong();
    }

    private long computeMask() {
        var excess = vpos+ LEN -limit;
        var v = excess <= 0
                ? ByteVector.fromMemorySegment(SPECIES, getMemorySegment(), vpos, ByteOrder.BIG_ENDIAN)
                : ByteVector.fromMemorySegment(SPECIES, getMemorySegment(), vpos, ByteOrder.BIG_ENDIAN, VectorMask.fromLong(SPECIES, FULL_MASK >>> excess));
        // Seems to be slightly faster than comparing against a scalar:
        return v.compare(VectorOperators.EQ, eolVector).toLong();
    }
}
