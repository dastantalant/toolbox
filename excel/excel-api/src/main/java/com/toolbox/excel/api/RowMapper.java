package com.toolbox.excel.api;

import com.toolbox.excel.api.context.DeserializationContext;

public interface RowMapper<T> {

    T map(RowData rowData, DeserializationContext context);

}