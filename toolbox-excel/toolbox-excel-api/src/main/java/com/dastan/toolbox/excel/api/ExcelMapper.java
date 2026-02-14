package com.dastan.toolbox.excel.api;

import com.dastan.toolbox.excel.api.config.Constants;
import com.dastan.toolbox.excel.api.config.ExcelConfig;
import com.dastan.toolbox.excel.api.config.ExcelFeature;
import com.dastan.toolbox.excel.api.context.DeserializationContext;
import com.dastan.toolbox.excel.api.parser.ParserRegistry;
import com.dastan.toolbox.excel.api.reflect.ReflectionRowMapper;
import com.dastan.toolbox.excel.api.validation.HeaderValidator;
import com.dastan.toolbox.excel.api.validation.ValidatingRowMapper;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class ExcelMapper {
    private final Set<ExcelFeature> features;
    private final ParserRegistry parserRegistry;
    private Locale locale;
    private Validator validator;

    public ExcelMapper() {
        ExcelConfig config = new ExcelConfig();
        features = config.getFeatures();
        parserRegistry = new ParserRegistry();
        locale = Locale.US;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        } catch (Exception e) {
            validator = null;
        }
    }

    public ExcelMapper enable(ExcelFeature feature) {
        features.add(feature);
        return this;
    }

    public ExcelMapper disable(ExcelFeature feature) {
        features.remove(feature);
        return this;
    }

    public ExcelMapper setLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public <T> void read(RowStreamSource source, Class<T> type, ExcelReadListener<T> listener) {
        DeserializationContext context = new DeserializationContext(features, parserRegistry, locale);
        RowMapper<T> mapper = getMapper(type, context);

        try (Stream<RowData> stream = source.stream()) {
            Iterator<RowData> iterator = stream.iterator();
            boolean firstRow = true;

            while (iterator.hasNext()) {
                RowData row = iterator.next();

                try {
                    if (firstRow) {
                        firstRow = false;
                        if (features.contains(ExcelFeature.FAIL_ON_MISSING_HEADER)) {
                            HeaderValidator.validate(type, row);
                        }
                        if (features.contains(ExcelFeature.SKIP_HEADER_ROW)) {
                            continue;
                        }
                    }
                    T instance = mapper.map(row, context);
                    listener.onRead(instance, row);
                } catch (Exception e) {
                    listener.onError(e, row);
                    if (features.contains(ExcelFeature.FAIL_ON_ERROR)) {
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> RowMapper<T> getMapper(Class<T> type, DeserializationContext context) {
        RowMapper<T> baseMapper;

        try {
            String mapperName = type.getName() + Constants.MAPPER_POSTFIX;
            Class<?> mapperClass = Class.forName(mapperName);
            baseMapper = (RowMapper<T>) mapperClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            baseMapper = new ReflectionRowMapper<>(type, context);
        }

        if (validator != null) {
            return new ValidatingRowMapper<>(baseMapper, validator);
        }

        return baseMapper;
    }
}