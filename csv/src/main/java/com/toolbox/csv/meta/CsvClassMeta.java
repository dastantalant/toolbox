package com.toolbox.csv.meta;

import java.lang.invoke.MethodHandle;
import java.util.List;

public final class CsvClassMeta {
    private final List<CsvFieldMeta> fields;
    private final MethodHandle constructor;
    private final boolean isRecord;
    private final int recordComponentCount;

    public CsvClassMeta(List<CsvFieldMeta> fields, MethodHandle constructor, boolean isRecord, int recordComponentCount) {
        this.fields = fields;
        this.constructor = constructor;
        this.isRecord = isRecord;
        this.recordComponentCount = recordComponentCount;
    }

    public List<CsvFieldMeta> getFields() {
        return fields;
    }

    public MethodHandle getConstructor() {
        return constructor;
    }

    public boolean isRecord() {
        return isRecord;
    }

    public int getRecordComponentCount() {
        return recordComponentCount;
    }
}