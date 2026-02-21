package com.toolbox.csv;

public class MetadataCacheException extends RuntimeException {
    public MetadataCacheException(String message) {
        super(message);
    }

    public MetadataCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetadataCacheException(Throwable cause) {
        super(cause);
    }

    public MetadataCacheException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MetadataCacheException() {
    }
}
