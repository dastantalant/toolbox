package com.toolbox.excel.api.context;

import com.toolbox.excel.api.CellParser;
import com.toolbox.excel.api.config.ExcelFeature;
import com.toolbox.excel.api.parser.ParserRegistry;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public class DeserializationContext {
    private final EnumSet<ExcelFeature> features;
    private final ParserRegistry registry;
    private final Locale locale;
    private final ThreadLocal<String> currentFormat = new ThreadLocal<>();

    public DeserializationContext(
            Set<ExcelFeature> features,
            ParserRegistry registry,
            Locale locale
    ) {
        this.features = features instanceof EnumSet && !features.isEmpty()
                ? EnumSet.copyOf(features)
                : EnumSet.noneOf(ExcelFeature.class);
        this.registry = registry;
        this.locale = locale;
    }

    public boolean isEnabled(ExcelFeature feature) {
        return features.contains(feature);
    }

    public <T> CellParser<T> findParser(Class<T> type) {
        return registry.get(type);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setFormat(String format) {
        if (format == null || format.isEmpty()) currentFormat.remove();
        else currentFormat.set(format);
    }

    public String getFormat() {
        return currentFormat.get();
    }
}