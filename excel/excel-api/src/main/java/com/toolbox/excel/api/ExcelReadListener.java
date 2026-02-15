package com.toolbox.excel.api;

public interface ExcelReadListener<T> {

    void onRead(T item, RowData rowData);

    default void onError(Exception e, RowData rowData) {
        throw new RuntimeException("Error processing row " + (rowData != null ? rowData.getRowIndex() : "?"), e);
    }
}