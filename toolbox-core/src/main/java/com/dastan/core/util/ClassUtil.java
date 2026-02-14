package com.dastan.core.util;


public final class ClassUtil {
    private ClassUtil() {}

    public static String getSetterName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return "";
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    public static boolean isPrimitive(String typeName) {
        return typeName.equals("int") || typeName.equals("long") ||
                typeName.equals("double") || typeName.equals("float") ||
                typeName.equals("boolean") || typeName.equals("byte") ||
                typeName.equals("short");
    }
}