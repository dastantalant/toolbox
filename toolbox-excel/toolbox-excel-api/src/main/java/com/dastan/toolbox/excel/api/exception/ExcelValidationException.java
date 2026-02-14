package com.dastan.toolbox.excel.api.exception;

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

    public int getRowIndex() { return rowIndex; }
    public Set<? extends ConstraintViolation<?>> getViolations() { return violations; }

    private static String buildMessage(int rowIndex, Set<? extends ConstraintViolation<?>> violations) {
        String details = violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(", "));
        return "Validation failed at row " + rowIndex + ": " + details;
    }
}