package com.toolbox.core.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public final class CsvWriter<T> {

    private final byte sep;
    private final byte nl = '\n';
    private final List<CsvFieldMeta> fields;
    private final boolean writeHeader;

    public CsvWriter(Class<T> type) {
        CsvFile cfg = type.getAnnotation(CsvFile.class);
        sep = (cfg != null ? cfg.separator() : Separator.COMMA).byteValue();
        writeHeader = cfg == null || cfg.header();
        fields = MetadataCache.get(type).fields;
    }

    public void write(List<T> items, OutputStream out) throws Throwable {
        if (writeHeader) {
            writeHeader(out);
        }
        for (T item : items) {
            writeRow(item, out);
        }
    }

    private void writeHeader(OutputStream out) throws IOException {
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) out.write(sep);
            Utf8Writer.writeEscaped(out, fields.get(i).header, sep);
        }
        out.write(nl);
    }

    private void writeRow(T dto, OutputStream out) throws Throwable {
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) out.write(sep);
            CsvFieldMeta meta = fields.get(i);
            Object raw = meta.getter.invoke(dto);
            Object converted = meta.converter.convert(raw);
            if (converted != null) {
                Utf8Writer.writeEscaped(out, converted.toString(), sep);
            }
        }
        out.write(nl);
    }
}