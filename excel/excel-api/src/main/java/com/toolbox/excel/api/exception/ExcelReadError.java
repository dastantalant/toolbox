package com.toolbox.excel.api.exception;

import com.toolbox.excel.api.RowData;

public record ExcelReadError(int rowIndex, String colName, Throwable cause, RowData rawData) {}