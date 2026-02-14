package com.dastan.core.converter;

@FunctionalInterface
public interface Converter<S, R> {
    R convert(S source);
}
