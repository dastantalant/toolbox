package com.toolbox.io.text.output;

import com.toolbox.io.buffer.BufferedOutput;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/// TextOutput prints formatted text to a [BufferedOutput]. It generally behaves like
/// [java.io.PrintWriter] except that it does not swallow IOExceptions. They are thrown
/// immediately like in BufferedOutput/OutputStream/etc. Most methods return `this` for
/// convenient chaining of operations. Unlike PrintWriter/PrintStream it allows the end-of-line
/// sequence to be changed from the system default.
///
/// This class is not thread-safe. Unless external synchronization is used, an instance may only
/// be accessed from a single thread (which must be the same thread that is used for accessing
/// the underlying BufferedOutput).
///
/// The standard Charsets [StandardCharsets#ISO_8859_1], [StandardCharsets#UTF_8] and
/// [StandardCharsets#US_ASCII] are specially optimized and much faster than the generic
/// implementation for other Charsets (which is still significantly faster than
/// [java.io.PrintWriter] because it can write directly to a BufferedOutput).
///
/// TextOutput does not perform any internal buffering, so you can mix calls to a TextOutput and the underlying
/// BufferedOutput instance. The only general exception to this is when a surrogate pair is split (i.e. the high and
/// low surrogate character are written in 2 separate [#print(String)] or [#print(char)] calls), but more esoteric
/// Charset implementations may contain buffering of their own.
///
/// TextOutput supports optional auto-flushing similar to [java.io.PrintWriter]. It is initially
/// disabled for all newly-created TextOutput objects and can be changed by calling
/// [#autoFlush(boolean)]. This flushes the BufferedOutput after any of the `println` methods is
/// called.
///
/// A TextOutput is created by calling [BufferedOutput#text(Charset, String)] or one of its
/// overloads.
public abstract class TextOutput implements Closeable, Flushable {

    public static TextOutput of(BufferedOutput output, Charset charset, String eol) {
        if (charset == StandardCharsets.ISO_8859_1) return new Latin1TextOutput(output, eol.getBytes(charset));
        else if (charset == StandardCharsets.UTF_8) return new UTF8TextOutput(output, eol.getBytes(charset));
        else if (charset == StandardCharsets.US_ASCII) return new ASCIITextOutput(output, eol.getBytes(charset));
        else return new GenericTextOutput(output, charset, eol);
    }

    private final BufferedOutput output;
    private final Charset charset;
    private boolean autoFlush = false;
    protected TextOutput(BufferedOutput output, Charset charset) {this.output = output;this.charset = charset;}
    public BufferedOutput getOutput() {return output;}
    public Charset getCharset() {return charset;}
    public TextOutput autoFlush(boolean autoFlush) {this.autoFlush = autoFlush;return this;}
    public boolean autoFlush() {return autoFlush;}
    protected final CharBuffer surrogateCharBuf = CharBuffer.allocate(2);
    protected boolean hasSurrogate = false;
    protected abstract void printRaw(String s) throws IOException;
    protected abstract void printlnRaw(String s) throws IOException;
    protected abstract void printlnRaw() throws IOException;
    protected abstract void printRawNull() throws IOException;
    protected abstract void printlnRawNull() throws IOException;
    protected  final void maybeFlush() throws IOException {if (autoFlush) output.flush();}
    public final TextOutput println() throws IOException {printlnRaw();maybeFlush();return this;}
    public final TextOutput print(String s) throws IOException {
        if (s == null) printRawNull();
        else printRaw(s);
        return this;
    }
    public final TextOutput println(String s) throws IOException {
        if (s == null) printlnRawNull();
        else printlnRaw(s);
        maybeFlush();
        return this;
    }
    public final TextOutput print(Object o) throws IOException {
        if (o == null) printRawNull();
        else printRaw(String.valueOf(o));
        return this;
    }
    public final TextOutput println(Object o) throws IOException {
        if (o == null) printlnRawNull();
        else printlnRaw(String.valueOf(o));
        maybeFlush();
        return this;
    }
    public TextOutput print(int i) throws IOException {printRaw(Integer.toString(i));return this;}
    public TextOutput println(int i) throws IOException {
        printlnRaw(Integer.toString(i));
        maybeFlush();
        return this;
    }
    public TextOutput print(boolean b) throws IOException {printRaw(String.valueOf(b));return this;}
    public TextOutput println(boolean b) throws IOException {
        printlnRaw(String.valueOf(b));
        maybeFlush();
        return this;
    }
    public TextOutput print(long l) throws IOException {printRaw(Long.toString(l));return this;}
    public TextOutput println(long l) throws IOException {
        printlnRaw(Long.toString(l));
        maybeFlush();
        return this;
    }
    public TextOutput print(char c) throws IOException {printRaw(String.valueOf(c));return this;}
    public TextOutput println(char c) throws IOException {
        printlnRaw(String.valueOf(c));
        maybeFlush();
        return this;
    }
    public TextOutput print(char[] s) throws IOException {printRaw(new String(s));return this;}
    public TextOutput println(char[] s) throws IOException {
        printlnRaw(new String(s));
        maybeFlush();
        return this;
    }
    public TextOutput print(float f) throws IOException {printRaw(String.valueOf(f));return this;}
    public TextOutput println(float f) throws IOException {
        printlnRaw(String.valueOf(f));
        maybeFlush();
        return this;
    }
    public TextOutput print(double d) throws IOException {printRaw(String.valueOf(d));return this;}
    public TextOutput println(double d) throws IOException {
        printlnRaw(String.valueOf(d));
        maybeFlush();
        return this;
    }
    public TextOutput print(double d, int precision) throws IOException {printRaw(String.format("%." + precision + "f", d));return this;}
    public TextOutput println(double d, int precision) throws IOException {
        printlnRaw(String.format("%." + precision + "f", d));
        maybeFlush();
        return this;
    }
    public TextOutput print(float d, int precision) throws IOException {print((double) d, precision);return this;}
    public TextOutput println(float d, int precision) throws IOException {println((double) d, precision);return this;}
    public void flush() throws IOException {output.flush();}
    public void close() throws IOException {output.close();}
}
