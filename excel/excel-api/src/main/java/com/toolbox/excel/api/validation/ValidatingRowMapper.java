package com.toolbox.excel.api.validation;

import com.toolbox.excel.api.RowData;
import com.toolbox.excel.api.RowMapper;
import com.toolbox.excel.api.context.DeserializationContext;
import com.toolbox.excel.api.exception.ExcelValidationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Set;

public class ValidatingRowMapper<T> implements RowMapper<T> {
    private final RowMapper<T> delegate;
    private final Validator validator;

    public ValidatingRowMapper(RowMapper<T> delegate, Validator validator) {
        this.delegate = delegate;
        this.validator = validator;
    }

    @Override
    public T map(RowData row, DeserializationContext ctx) {
        T object = delegate.map(row, ctx);
        if (object != null) {
            Set<ConstraintViolation<T>> violations = validator.validate(object);
            if (!violations.isEmpty()) throw new ExcelValidationException(row.getRowIndex(), violations);
        }
        return object;
    }
}