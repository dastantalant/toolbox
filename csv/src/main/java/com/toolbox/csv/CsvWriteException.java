package com.toolbox.csv;

public class CsvWriteException extends RuntimeException {
    public CsvWriteException(String message) {
        super(message);
    }

    public CsvWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsvWriteException(Throwable cause) {
        super(cause);
    }

    public CsvWriteException() {
    }
}
