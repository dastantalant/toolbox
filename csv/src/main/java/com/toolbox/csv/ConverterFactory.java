package com.toolbox.csv;


import com.toolbox.core.collection.ConcurrentFixedLruMap;
import com.toolbox.core.converter.Converter;
import com.toolbox.core.converter.IdentityConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConverterFactory {
    private ConverterFactory() {}
    private static final Map<Class<?>, Converter<?, ?>> CACHE = new ConcurrentFixedLruMap<>(200);
    @SuppressWarnings("unchecked")
    public static Converter<Object, Object> get(Class<? extends Converter<?, ?>> type) {
        return (Converter<Object, Object>) CACHE.computeIfAbsent(type, cls -> {
            if (cls == IdentityConverter.class) return IdentityConverter.INSTANCE;
            try {
                return (Converter<Object, Object>) cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Cannot instantiate converter: " + cls, e);
            }
        });
    }
}
