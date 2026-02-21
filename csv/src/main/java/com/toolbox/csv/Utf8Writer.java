package com.toolbox.csv;

import java.io.IOException;
import java.io.OutputStream;

final class Utf8Writer {
    private Utf8Writer() {}
    static void write(OutputStream out, CharSequence cs) throws IOException {
        for (int i = 0; i < cs.length(); i++) {
            char c = cs.charAt(i);
            if (c < 0x80) {
                out.write(c);
            } else if (c < 0x800) {
                out.write(0xC0 | (c >> 6));
                out.write(0x80 | (c & 0x3F));
            } else {
                out.write(0xE0 | (c >> 12));
                out.write(0x80 | ((c >> 6) & 0x3F));
                out.write(0x80 | (c & 0x3F));
            }
        }
    }
    static void writeEscaped(OutputStream out, CharSequence value, byte sep) throws IOException {
        boolean quote = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\n' || c == sep) {
                quote = true;
                break;
            }
        }
        if (!quote) {
            Utf8Writer.write(out, value);
            return;
        }
        out.write('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"') {
                out.write('"');
            }

            if (c < 0x80) {
                out.write(c);
            } else if (c < 0x800) {
                out.write(0xC0 | (c >> 6));
                out.write(0x80 | (c & 0x3F));
            } else {
                out.write(0xE0 | (c >> 12));
                out.write(0x80 | ((c >> 6) & 0x3F));
                out.write(0x80 | (c & 0x3F));
            }
        }
        out.write('"');
    }
}
