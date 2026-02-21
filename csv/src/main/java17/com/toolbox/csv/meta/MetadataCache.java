package com.toolbox.csv.meta;

import com.toolbox.core.collection.ConcurrentFixedLruMap;
import com.toolbox.csv.MetadataCacheException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MetadataCache {
    private MetadataCache() {
    }

    private static final Map<Class<?>, CsvClassMeta> CACHE = new ConcurrentFixedLruMap<>(200);
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static CsvClassMeta get(Class<?> type) {
        return CACHE.computeIfAbsent(type, MetadataCache::scan);
    }

    private static CsvClassMeta scan(Class<?> clazz) {
        if (!clazz.isRecord()) return MetadataScannerHelper.scanPojo(clazz, LOOKUP);
        List<CsvFieldMeta> fieldMetas = new ArrayList<>();
        MethodHandle constructorHandle;
        int componentCount;
        try {
            RecordComponent[] components = clazz.getRecordComponents();
            componentCount = components.length;
            Map<String, Integer> componentIndexMap = new HashMap<>();
            Class<?>[] paramTypes = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) {
                componentIndexMap.put(components[i].getName(), i);
                paramTypes[i] = components[i].getType();
            }
            constructorHandle = LOOKUP.findConstructor(clazz, MethodType.methodType(void.class, paramTypes));
            for (Field field : clazz.getDeclaredFields()) {
                if (MetadataScannerHelper.shouldIgnore(field)) continue;
                field.setAccessible(true);
                MethodHandle getter = LOOKUP.unreflectGetter(field);
                Integer idx = componentIndexMap.get(field.getName());
                int naturalOrder = (idx != null) ? idx : Integer.MAX_VALUE;
                int constrIdx = (idx != null) ? idx : -1;
                fieldMetas.add(MetadataScannerHelper.createMeta(field, getter, null, constrIdx, naturalOrder));
                field.setAccessible(false);
            }
        } catch (Exception e) {
            throw new MetadataCacheException("Failed to analyze Record class: " + clazz.getName(), e);
        }
        return MetadataScannerHelper.buildClassMeta(fieldMetas, constructorHandle, true, componentCount);
    }
}