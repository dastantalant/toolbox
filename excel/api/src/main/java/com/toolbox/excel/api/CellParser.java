package com.toolbox.excel.api;

import com.toolbox.excel.api.context.DeserializationContext;

public interface CellParser<T> {

    T parse(String value, DeserializationContext context);

    interface None extends CellParser<Object> {
        @Override
        default Object parse(String value, DeserializationContext context) {
            return null;
        }
    }
}