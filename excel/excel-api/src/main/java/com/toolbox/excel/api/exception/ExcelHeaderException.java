package com.toolbox.excel.api.exception;

import java.util.Set;

public class ExcelHeaderException extends ExcelException {
    public ExcelHeaderException(Set<String> missingHeaders) {
        super("Missing required headers: " + String.join(", ", missingHeaders));
    }

    public ExcelHeaderException(String message) {
        super(message);
    }

    public ExcelHeaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelHeaderException(Throwable cause) {
        super(cause);
    }

    public ExcelHeaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ExcelHeaderException() {
    }
}