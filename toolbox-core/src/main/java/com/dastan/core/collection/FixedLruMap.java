package com.dastan.core.collection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Least Recently Used
 */
public class FixedLruMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public FixedLruMap(int maxSize) {
        super(maxSize + 1, 1.0f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}