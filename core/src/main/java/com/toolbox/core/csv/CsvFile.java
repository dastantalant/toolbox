package com.toolbox.core.csv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CsvFile {

    Separator separator() default Separator.COMMA;

    boolean header() default true;

    String lineSeparator() default "\n";
}
