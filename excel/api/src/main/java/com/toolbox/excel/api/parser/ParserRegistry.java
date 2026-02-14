package com.toolbox.excel.api.parser;

import com.toolbox.excel.api.CellParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParserRegistry {
    private final Map<Class<?>, CellParser<?>> parsers = new ConcurrentHashMap<>();

    public ParserRegistry() {
        DefaultParsers.registerAll(parsers);
    }

    public <T> void register(Class<T> type, CellParser<T> parser) {
        parsers.put(type, parser);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> CellParser<T> get(Class<T> type) {
        CellParser<T> parser = (CellParser<T>) parsers.get(type);
        if (parser != null) return parser;

        if (type.isEnum()) {
            EnumParser enumParser = new EnumParser(type);
            parsers.put(type, enumParser);
            return (CellParser<T>) enumParser;
        }
        return null;
    }
}