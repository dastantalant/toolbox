package com.toolbox.csv.meta;

import com.toolbox.core.converter.Converter;

import java.lang.invoke.MethodHandle;

public final class CsvFieldMeta {
    private final String header;
    private final int order;
    private final Class<?> type;
    private final MethodHandle getter;
    private final MethodHandle setter;
    private final int constructorParamIndex;
    private final Converter<Object, Object> converter;

    CsvFieldMeta(
            String header,
            int order,
            Class<?> type,
            MethodHandle getter,
            MethodHandle setter,
            int constructorParamIndex,
            Converter<Object, Object> converter
    ) {
        this.header = header;
        this.order = order;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
        this.constructorParamIndex = constructorParamIndex;
        this.converter = converter;
    }

    public String getHeader() {
        return header;
    }

    public int getOrder() {
        return order;
    }

    public Class<?> getType() {
        return type;
    }

    public MethodHandle getGetter() {
        return getter;
    }

    public MethodHandle getSetter() {
        return setter;
    }

    public int getConstructorParamIndex() {
        return constructorParamIndex;
    }

    public Converter<Object, Object> getConverter() {
        return converter;
    }
}