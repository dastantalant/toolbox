package com.dastan.core.collection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * First In, First Out
 */
public class FixedFifoMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public FixedFifoMap(int maxSize) {
        super(maxSize + 1, 1.0f, false);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}