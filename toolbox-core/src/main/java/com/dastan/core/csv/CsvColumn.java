package com.dastan.core.csv;

import com.dastan.core.converter.Converter;
import com.dastan.core.converter.IdentityConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {
    String value() default "";
    int order() default Integer.MAX_VALUE;
    Class<? extends Converter<?, ?>> converter() default IdentityConverter.class;
}
