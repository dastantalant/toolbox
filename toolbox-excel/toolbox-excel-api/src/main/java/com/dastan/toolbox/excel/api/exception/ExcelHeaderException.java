package com.dastan.toolbox.excel.api.exception;

import java.util.Set;

public class ExcelHeaderException extends ExcelException {
    public ExcelHeaderException(Set<String> missingHeaders) {
        super("Missing required headers: " + String.join(", ", missingHeaders));
    }
}