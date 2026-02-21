package com.toolbox.core.util;


public final class ClassUtil {
    private ClassUtil() {
    }

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

    public static String getPackageName(Class<?> clazz) {
        if (clazz == null) throw new IllegalArgumentException("clazz == null");
        Package pkg = clazz.getPackage();
        if (pkg != null) return pkg.getName();
        String className = clazz.getName();
        int lastDot = className.lastIndexOf('.');
        return (lastDot >= 0) ? className.substring(0, lastDot) : "";
    }
}