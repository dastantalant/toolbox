package com.dastan.toolbox.excel.api.config;

import java.util.EnumSet;
import java.util.Set;

public class ExcelConfig {
    private final Set<ExcelFeature> features = EnumSet.noneOf(ExcelFeature.class);

    public ExcelConfig() {
        for (ExcelFeature f : ExcelFeature.values()) {
            if (f.enabledByDefault()) features.add(f);
        }
    }

    public Set<ExcelFeature> getFeatures() {return features;}
    public void enable(ExcelFeature feature) { features.add(feature); }
    public void disable(ExcelFeature feature) { features.remove(feature); }
    public boolean isEnabled(ExcelFeature feature) { return features.contains(feature); }
}