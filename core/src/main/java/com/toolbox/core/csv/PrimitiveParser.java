package com.toolbox.core.csv;

import java.math.BigDecimal;

final class PrimitiveParser {

    @SuppressWarnings("unchecked")
    static Object parse(String value, Class<?> targetType) {
        if (value == null || value.isEmpty()) return null;
        if (targetType == String.class) return value;

        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
        if (targetType == BigDecimal.class) return new BigDecimal(value);
        if (targetType.isEnum()) return Enum.valueOf((Class<Enum>) targetType, value);

        return value;
    }
}