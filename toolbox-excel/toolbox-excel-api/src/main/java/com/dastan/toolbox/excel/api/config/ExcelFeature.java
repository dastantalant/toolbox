package com.dastan.toolbox.excel.api.config;

public enum ExcelFeature {
    TRIM_CELL_VALUES(true),
    IGNORE_UNKNOWN_COLUMNS(true),
    FAIL_ON_ERROR(false),
    FAIL_ON_MISSING_HEADER(false),
    SKIP_HEADER_ROW(true)
    ;

    private final boolean defaultState;

    ExcelFeature(boolean defaultState) {
        this.defaultState = defaultState;
    }

    public boolean enabledByDefault() {
        return defaultState;
    }
}
