package com.toolbox.csv.meta;

import com.toolbox.core.converter.Converter;
import com.toolbox.core.converter.IdentityConverter;
import com.toolbox.csv.ConverterFactory;
import com.toolbox.csv.annotation.CsvColumn;
import com.toolbox.csv.annotation.CsvIgnore;
import com.toolbox.csv.MetadataCacheException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class MetadataScannerHelper {
    private MetadataScannerHelper() {}

    static CsvClassMeta scanPojo(Class<?> clazz, MethodHandles.Lookup lookup) {
        List<CsvFieldMeta> fieldMetas = new ArrayList<>();
        MethodHandle constructorHandle;
        try {
            constructorHandle = lookup.findConstructor(clazz, MethodType.methodType(void.class));
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (shouldIgnore(field)) continue;
                field.setAccessible(true);
                MethodHandle getter = lookup.unreflectGetter(field);
                MethodHandle setter = lookup.unreflectSetter(field);
                fieldMetas.add(createMeta(field, getter, setter, -1, i));
                field.setAccessible(false);
            }
        } catch (Exception e) {
            throw new MetadataCacheException("Failed to analyze POJO class: " + clazz.getName(), e);
        }
        return buildClassMeta(fieldMetas, constructorHandle, false, 0);
    }

    static boolean shouldIgnore(Field field) {
        return Modifier.isStatic(field.getModifiers()) || field.isAnnotationPresent(CsvIgnore.class);
    }

    @SuppressWarnings("unchecked")
    static CsvFieldMeta createMeta(Field field, MethodHandle getter, MethodHandle setter, int constrIdx, int defaultOrder) throws Exception {
        CsvColumn annotation = field.getAnnotation(CsvColumn.class);
        String name = (annotation != null && !annotation.value().isEmpty()) ? annotation.value() : field.getName();
        int order = (annotation != null && annotation.order() != 0) ? annotation.order() : defaultOrder;
        Class<?> converterClass = (annotation != null) ? annotation.converter() : IdentityConverter.class;
        Converter<Object, Object> converter = ConverterFactory.get((Class<? extends Converter<?, ?>>) converterClass);
        return new CsvFieldMeta(name, order, field.getType(), getter, setter, constrIdx, converter);
    }

    static CsvClassMeta buildClassMeta(List<CsvFieldMeta> fieldMetas, MethodHandle constructor, boolean isRecord, int componentCount) {
        fieldMetas.sort(Comparator.comparingInt(CsvFieldMeta::getOrder).thenComparing(CsvFieldMeta::getHeader));
        return new CsvClassMeta(fieldMetas, constructor, isRecord, componentCount);
    }
}