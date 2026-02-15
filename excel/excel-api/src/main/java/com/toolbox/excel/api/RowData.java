package com.toolbox.excel.api;

public interface RowData {

    int getRowIndex();

    String get(int index);

    String get(String name);

    boolean hasColumn(String name);
}