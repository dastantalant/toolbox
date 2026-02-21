package com.toolbox.io.text.tokenizer;

import com.toolbox.io.buffer.BufferedInput;
import com.toolbox.io.util.VectorSupport;
import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A vectorized implementation of [[DirectLineTokenizer]].
 * <p>
 * The parser uses explicit SIMD loops with up to 512 bits / 64 lanes per vector (depending on
 * hardware and JVM support).
 */
public abstract class DirectVectorizedLineTokenizer extends DirectLineTokenizer {

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
        this.vpos = getStart() - VectorSupport.LEN;
        this.limit = getMemorySegment().byteSize();
        this.eolVector = ByteVector.broadcast(VectorSupport.SPECIES, eolChar);
    }

    @Override
    public void markClosed() {
        super.markClosed();
        mask = 0L;
        vpos = Long.MAX_VALUE - VectorSupport.LEN;
    }

    private String rest() throws IOException {
        String r;
        if (vpos != Long.MAX_VALUE) {
            long l = limit - getStart();
            mask = 0;
            if (l != 0) {
                String s = makeString(getMemorySegment(), getStart(), l);
                setStart(limit);
                r = s;
            } else r = null;
        } else {
            checkState();
            r = null;
        }
        vpos = Long.MAX_VALUE - VectorSupport.LEN;
        return r;
    }

    public String readLine() throws IOException {
        while (true) {
            long f = Long.numberOfTrailingZeros(mask);
            if (f < VectorSupport.LEN) {
                long lfpos = vpos + f;
                String s = emit(getStart(), lfpos);
                setStart(lfpos + 1);
                mask &= ~(1L << f);
                return s;
            }
            vpos += VectorSupport.LEN;
            if (vpos <= limit - VectorSupport.LEN) mask = computeSimpleMask();
            else if (vpos < limit) mask = computeMask();
            else return rest();
        }
    }

    private long computeSimpleMask() {
        return ByteVector.fromMemorySegment(VectorSupport.SPECIES, getMemorySegment(), vpos, ByteOrder.BIG_ENDIAN).compare(VectorOperators.EQ, eolVector).toLong();
    }

    private long computeMask() {
        long excess = vpos + VectorSupport.LEN - limit;
        ByteVector v = excess <= 0
                ? ByteVector.fromMemorySegment(VectorSupport.SPECIES, getMemorySegment(), vpos, ByteOrder.BIG_ENDIAN)
                : ByteVector.fromMemorySegment(VectorSupport.SPECIES, getMemorySegment(), vpos, ByteOrder.BIG_ENDIAN, VectorMask.fromLong(VectorSupport.SPECIES, VectorSupport.FULL_MASK >>> excess));
        // Seems to be slightly faster than comparing against a scalar:
        return v.compare(VectorOperators.EQ, eolVector).toLong();
    }
}
