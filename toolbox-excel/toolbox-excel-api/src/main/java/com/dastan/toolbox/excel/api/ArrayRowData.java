package com.dastan.toolbox.excel.api;

import java.util.Map;

public class ArrayRowData implements RowData {
    private final int rowIndex;
    private final String[] cells;
    private final Map<String, Integer> headers;

    public ArrayRowData(int rowIndex, String[] cells, Map<String, Integer> headers) {
        this.rowIndex = rowIndex;
        this.cells = cells;
        this.headers = headers;
    }

    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public String get(int index) {
        if (index < 0 || index >= cells.length) {
            return null;
        }
        return cells[index];
    }

    @Override
    public String get(String name) {
        if (headers == null) return null;
        Integer index = headers.get(name);
        return index != null ? get(index) : null;
    }

    @Override
    public boolean hasColumn(String name) {
        return headers != null && headers.containsKey(name);
    }
}