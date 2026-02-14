package com.dastan.core.csv;

import com.dastan.core.converter.Converter;
import com.dastan.core.converter.IdentityConverter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class MetadataCache {
    private static final Map<Class<?>, CsvClassMeta> CACHE = new ConcurrentHashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    static CsvClassMeta get(Class<?> type) {
        return CACHE.computeIfAbsent(type, MetadataCache::scan);
    }

    private static CsvClassMeta scan(Class<?> clazz) {
        boolean isRecord = clazz.isRecord();
        List<CsvFieldMeta> fieldMetas = new ArrayList<>();
        MethodHandle constructorHandle;
        int componentCount = 0;

        try {
            if (isRecord) {
                RecordComponent[] components = clazz.getRecordComponents();
                componentCount = components.length;

                // Карта: Имя поля -> Его реальный индекс в конструкторе (0, 1, 2...)
                Map<String, Integer> componentIndexMap = new HashMap<>();
                Class<?>[] paramTypes = new Class<?>[components.length];
                for (int i = 0; i < components.length; i++) {
                    componentIndexMap.put(components[i].getName(), i);
                    paramTypes[i] = components[i].getType();
                }

                constructorHandle = LOOKUP.findConstructor(clazz, MethodType.methodType(void.class, paramTypes));

                for (Field field : clazz.getDeclaredFields()) {
                    if (shouldIgnore(field)) continue;

                    field.setAccessible(true);
                    MethodHandle getter = LOOKUP.unreflectGetter(field);

                    // Получаем индекс поля из карты компонентов
                    Integer idx = componentIndexMap.get(field.getName());

                    // Если поле есть в компонентах, его индекс - это его naturalOrder
                    // Если это вспомогательное поле (не в конструкторе), ставим в конец
                    int naturalOrder = (idx != null) ? idx : Integer.MAX_VALUE;
                    int constrIdx = (idx != null) ? idx : -1;

                    // Передаем naturalOrder в createMeta
                    fieldMetas.add(createMeta(field, getter, null, constrIdx, naturalOrder));
                }

            } else {
                constructorHandle = LOOKUP.findConstructor(clazz, MethodType.methodType(void.class));

                Field[] fields = clazz.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    if (shouldIgnore(field)) continue;

                    field.setAccessible(true);
                    MethodHandle getter = LOOKUP.unreflectGetter(field);
                    MethodHandle setter = LOOKUP.unreflectSetter(field);

                    // Для обычных классов используем индекс цикла как naturalOrder
                    fieldMetas.add(createMeta(field, getter, setter, -1, i));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze class: " + clazz.getName(), e);
        }

        // Сортировка: Сначала по order, затем по имени заголовка
        fieldMetas.sort(Comparator.comparingInt((CsvFieldMeta m) -> m.order).thenComparing(m -> m.header));

        return new CsvClassMeta(fieldMetas, constructorHandle, isRecord, componentCount);
    }

    private static boolean shouldIgnore(Field field) {
        return Modifier.isStatic(field.getModifiers()) || field.isAnnotationPresent(CsvIgnore.class);
    }

    // Добавлен параметр defaultOrder
    private static CsvFieldMeta createMeta(Field field, MethodHandle getter, MethodHandle setter, int constrIdx, int defaultOrder) throws Exception {
        CsvColumn annotation = field.getAnnotation(CsvColumn.class);
        String name = (annotation != null && !annotation.value().isEmpty()) ? annotation.value() : field.getName();

        // ЛОГИКА ОПРЕДЕЛЕНИЯ ПОРЯДКА:
        // 1. Если аннотации нет -> используем defaultOrder (порядок в коде).
        // 2. Если аннотация есть, но order не задан (равен 0 по дефолту) -> используем defaultOrder.
        // 3. Если order задан явно (> 0) -> используем его.

        int order;
        if (annotation == null) {
            order = defaultOrder;
        } else {
            // Предполагаем, что default value для order() в аннотации равно 0
            if (annotation.order() == 0) {
                order = defaultOrder;
            } else {
                order = annotation.order();
            }
        }

        Class<?> converterClass = (annotation != null) ? annotation.converter() : IdentityConverter.class;

        var converter = ConverterFactory.get((Class<? extends Converter<?, ?>>) converterClass);
        return new CsvFieldMeta(name, order, field.getType(), getter, setter, constrIdx, converter);
    }
}