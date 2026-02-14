package com.dastan.toolbox.excel.poi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StringPool {
    private final Map<String, String> pool = new ConcurrentHashMap<>();

    public String intern(String input) {
        if (input == null) return null;
        String cached = pool.putIfAbsent(input, input);
        return cached == null ? input : cached;
    }
}