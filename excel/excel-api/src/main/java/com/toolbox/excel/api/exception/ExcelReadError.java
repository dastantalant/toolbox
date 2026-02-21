package com.toolbox.excel.api.exception;

import com.toolbox.excel.api.RowData;

public class ExcelReadError {
    private int rowIndex;
    private String colName;
    private Throwable cause;
    private RowData rawData;

    public ExcelReadError(int rowIndex, String colName, Throwable cause, RowData rawData) {
        this.rowIndex = rowIndex;
        this.colName = colName;
        this.cause = cause;
        this.rawData = rawData;
    }

    public ExcelReadError() {
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int rowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public void rowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String getColName() {
        return colName;
    }

    public String colName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public void colName(String colName) {
        this.colName = colName;
    }

    public Throwable getCause() {
        return cause;
    }

    public Throwable cause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public void cause(Throwable cause) {
        this.cause = cause;
    }

    public RowData getRawData() {
        return rawData;
    }

    public RowData rawData() {
        return rawData;
    }

    public void setRawData(RowData rawData) {
        this.rawData = rawData;
    }

    public void rawData(RowData rawData) {
        this.rawData = rawData;
    }
}
