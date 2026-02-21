package com.toolbox.csv;

public class CsvReadException extends RuntimeException {
    public CsvReadException(String message) {
        super(message);
    }

    public CsvReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsvReadException(Throwable cause) {
        super(cause);
    }

    public CsvReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CsvReadException() {
    }
}
