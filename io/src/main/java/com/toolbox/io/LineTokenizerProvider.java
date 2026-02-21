package com.toolbox.io;

import java.io.IOException;
import java.nio.charset.Charset;

public interface LineTokenizerProvider {
    boolean isAvailable();

    int priority();

    LineTokenizer createTokenizer(BufferedInput in, Charset charset, byte eol, byte preEol) throws IOException;
}