package com.toolbox.excel.api.validation;

import com.toolbox.core.collection.ConcurrentFixedLruMap;
import com.toolbox.excel.api.RowData;
import com.toolbox.excel.api.annotation.ExcelColumn;
import com.toolbox.excel.api.exception.ExcelHeaderException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HeaderValidator {

    private static final Map<Class<?>, Set<String>> REQUIRED_HEADERS_CACHE = new ConcurrentFixedLruMap<>(100);

    private HeaderValidator() {}

    public static void validate(Class<?> type, RowData headerRow) {
        Set<String> expectedHeaders = REQUIRED_HEADERS_CACHE.computeIfAbsent(type, HeaderValidator::scanRequiredHeaders);
        if (expectedHeaders.isEmpty()) return;
        Set<String> missingHeaders = new HashSet<>();
        for (String header : expectedHeaders)
            if (!headerRow.hasColumn(header)) missingHeaders.add(header);
        if (!missingHeaders.isEmpty()) throw new ExcelHeaderException(missingHeaders);
    }

    private static Set<String> scanRequiredHeaders(Class<?> clazz) {
        Set<String> headers = new HashSet<>();
        for (Field field : clazz.getDeclaredFields()) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null && !annotation.value().isEmpty()) headers.add(annotation.value());
        }
        return headers.isEmpty() ? Collections.emptySet() : Set.copyOf(headers);
    }
}