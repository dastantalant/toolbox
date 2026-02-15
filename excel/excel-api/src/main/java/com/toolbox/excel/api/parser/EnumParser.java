package com.toolbox.excel.api.parser;

import com.toolbox.excel.api.CellParser;
import com.toolbox.excel.api.context.DeserializationContext;

public class EnumParser<E extends Enum<E>> implements CellParser<E> {
    private final Class<E> type;

    public EnumParser(Class<E> type) {
        this.type = type;
    }

    @Override
    public E parse(String value, DeserializationContext context) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim();
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(trimmed)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Unknown value '" + value + "' for enum " + type.getSimpleName());
    }
}