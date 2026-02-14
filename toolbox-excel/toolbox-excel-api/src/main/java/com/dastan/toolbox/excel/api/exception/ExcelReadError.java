package com.dastan.toolbox.excel.api.exception;

import com.dastan.toolbox.excel.api.RowData;

public record ExcelReadError(int rowIndex, String colName, Throwable cause, RowData rawData) {}