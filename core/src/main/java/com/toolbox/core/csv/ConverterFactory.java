package com.toolbox.core.csv;


import com.toolbox.core.converter.Converter;
import com.toolbox.core.converter.IdentityConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class ConverterFactory {

    private static final Map<Class<?>, Converter<?, ?>> CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    static Converter<Object, Object> get(Class<? extends Converter<?, ?>> type) {
        return (Converter<Object, Object>) CACHE.computeIfAbsent(type, cls -> {
            if (cls == IdentityConverter.class) {
                return IdentityConverter.INSTANCE;
            }
            try {
                return (Converter<Object, Object>) cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Cannot instantiate converter: " + cls, e);
            }
        });
    }
}
