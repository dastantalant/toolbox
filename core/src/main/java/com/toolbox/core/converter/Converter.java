package com.toolbox.core.converter;

@FunctionalInterface
public interface Converter<S, R> {
    R convert(S source);
}
