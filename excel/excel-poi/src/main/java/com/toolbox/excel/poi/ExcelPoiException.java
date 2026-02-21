package com.toolbox.excel.poi;

public class ExcelPoiException extends RuntimeException {
    public ExcelPoiException(String message) {
        super(message);
    }

    public ExcelPoiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelPoiException(Throwable cause) {
        super(cause);
    }

    public ExcelPoiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ExcelPoiException() {
    }
}
