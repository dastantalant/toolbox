package com.dastan.toolbox.excel.api;

import java.lang.reflect.Field;

public record FieldMeta(Field field, int index, String name, String format, CellParser<?> parser) {
}