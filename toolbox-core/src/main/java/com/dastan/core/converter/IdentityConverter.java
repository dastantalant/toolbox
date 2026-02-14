package com.dastan.core.converter;

public final class IdentityConverter implements Converter<Object, Object> {
    public static final IdentityConverter INSTANCE = new IdentityConverter();

    private IdentityConverter() {}

    @Override
    public Object convert(Object o) {
        return o;
    }
}