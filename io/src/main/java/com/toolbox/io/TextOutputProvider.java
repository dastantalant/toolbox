package com.toolbox.io;

import java.nio.charset.Charset;

public interface TextOutputProvider {
    boolean isAvailable();

    int priority();

    TextOutput createOptimized(BufferedOutput out, Charset charset, String eol);
}