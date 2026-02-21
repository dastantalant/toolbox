package com.toolbox.excel.api.exception;

import jakarta.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

public class ExcelValidationException extends ExcelException {
    private final int rowIndex;
    private final Set<? extends ConstraintViolation<?>> violations;

    public <T> ExcelValidationException(int rowIndex, Set<ConstraintViolation<T>> violations) {
        super(buildMessage(rowIndex, violations));
        this.rowIndex = rowIndex;
        this.violations = violations;
    }

    public ExcelValidationException(String message, int rowIndex, Set<? extends ConstraintViolation<?>> violations) {
        super(message);
        this.rowIndex = rowIndex;
        this.violations = violations;
    }

    public ExcelValidationException(String message, Throwable cause, int rowIndex, Set<? extends ConstraintViolation<?>> violations) {
        super(message, cause);
        this.rowIndex = rowIndex;
        this.violations = violations;
    }

    public ExcelValidationException(Throwable cause, int rowIndex, Set<? extends ConstraintViolation<?>> violations) {
        super(cause);
        this.rowIndex = rowIndex;
        this.violations = violations;
    }

    public ExcelValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int rowIndex, Set<? extends ConstraintViolation<?>> violations) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.rowIndex = rowIndex;
        this.violations = violations;
    }

    public int getRowIndex() { return rowIndex; }
    public Set<? extends ConstraintViolation<?>> getViolations() { return violations; }

    private static String buildMessage(int rowIndex, Set<? extends ConstraintViolation<?>> violations) {
        String details = violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(", "));
        return "Validation failed at row " + rowIndex + ": " + details;
    }
}