package com.toolbox.csv;

import com.toolbox.core.converter.IdentityConverter;
import com.toolbox.csv.annotation.CsvFile;
import com.toolbox.csv.meta.CsvClassMeta;
import com.toolbox.csv.meta.CsvFieldMeta;
import com.toolbox.csv.meta.MetadataCache;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public final class CsvReader<T> {

    private final char sep;
    private final CsvClassMeta classMeta;
    private final boolean hasHeader;

    public CsvReader(Class<T> type) {
        CsvFile cfg = type.getAnnotation(CsvFile.class);
        sep = (cfg != null ? cfg.separator() : Separator.COMMA).symbol();
        hasHeader = cfg == null || cfg.header();
        classMeta = MetadataCache.get(type);
    }

    public static <T> List<T> read(Class<T> type, InputStream in) {
        CsvReader<T> reader = new CsvReader<>(type);
        return reader.read(in);
    }

    public List<T> read(InputStream in) {
        List<T> result = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (firstLine && hasHeader) {
                    firstLine = false;
                    continue;
                }
                result.add(parseRow(line));
            }
        } catch (Throwable e) {
            throw new CsvReadException("Error reading CSV", e);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private T parseRow(String line) throws Throwable {
        List<String> tokens = splitLine(line);
        List<CsvFieldMeta> fields = classMeta.getFields();

        if (classMeta.isRecord()) {
            Object[] args = new Object[classMeta.getRecordComponentCount()];
            for (int i = 0; i < fields.size() && i < tokens.size(); i++) {
                CsvFieldMeta meta = fields.get(i);
                if (meta.getConstructorParamIndex() >= 0) {
                    args[meta.getConstructorParamIndex()] = convert(tokens.get(i), meta);
                }
            }
            return (T) classMeta.getConstructor().invokeWithArguments(args);
        } else {
            T instance = (T) classMeta.getConstructor().invoke();
            for (int i = 0; i < fields.size() && i < tokens.size(); i++) {
                CsvFieldMeta meta = fields.get(i);
                Object val = convert(tokens.get(i), meta);
                meta.getSetter().invoke(instance, val);
            }
            return instance;
        }
    }

    private Object convert(String token, CsvFieldMeta meta) {
        if (meta.getConverter().getClass() != IdentityConverter.class) {
            return meta.getConverter().convert(token);
        }
        return PrimitiveParser.parse(token, meta.getType());
    }

    private List<String> splitLine(String line) {
        List<String> tokens = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == sep && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens;
    }
}