package com.toolbox.io.text.tokenizer;

import com.toolbox.io.buffer.BufferedInput;

import java.io.IOException;
import java.nio.charset.Charset;

public interface LineTokenizerProvider {
    boolean isAvailable();

    int priority();

    LineTokenizer createTokenizer(BufferedInput in, Charset charset, byte eol, byte preEol) throws IOException;
}