package com.toolbox.io.text.output;

import com.toolbox.io.buffer.BufferedOutput;

import java.nio.charset.Charset;

public interface TextOutputProvider {
    boolean isAvailable();

    int priority();

    TextOutput createOptimized(BufferedOutput out, Charset charset, String eol);
}