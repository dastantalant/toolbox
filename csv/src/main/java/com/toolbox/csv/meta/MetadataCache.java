package com.toolbox.csv.meta;

import com.toolbox.core.collection.ConcurrentFixedLruMap;

import java.lang.invoke.MethodHandles;
import java.util.Map;

public final class MetadataCache {
    private MetadataCache() {}
    private static final Map<Class<?>, CsvClassMeta> CACHE = new ConcurrentFixedLruMap<>(200);
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    public static CsvClassMeta get(Class<?> type) {
        return CACHE.computeIfAbsent(type, MetadataCache::scan);
    }
    private static CsvClassMeta scan(Class<?> clazz) {
        return MetadataScannerHelper.scanPojo(clazz, LOOKUP);
    }
}