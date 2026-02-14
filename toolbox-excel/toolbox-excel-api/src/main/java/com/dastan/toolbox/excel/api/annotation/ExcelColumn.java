package com.dastan.toolbox.excel.api.annotation;

import com.dastan.toolbox.excel.api.CellParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelColumn {

    String value() default "";

    int index() default -1;

    String format() default "";

    Class<? extends CellParser> parser() default CellParser.None.class;
}