package com.dastan.toolbox.excel.api;

import com.dastan.toolbox.excel.api.context.DeserializationContext;

public interface RowMapper<T> {

    T map(RowData rowData, DeserializationContext context);

}