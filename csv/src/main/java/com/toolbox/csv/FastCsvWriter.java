package com.toolbox.csv;

import com.toolbox.csv.annotation.CsvFile;
import com.toolbox.csv.meta.CsvFieldMeta;
import com.toolbox.csv.meta.MetadataCache;
import com.toolbox.io.Factory;
import com.toolbox.io.Output;

import java.io.Flushable;
import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class FastCsvWriter<T> implements Flushable, AutoCloseable {
    private final Output out;
    private final char sep;
    private final List<CsvFieldMeta> fields;
    private final boolean writeHeader;

    public FastCsvWriter(Class<T> type, Output out) {
        this.out = out;
        CsvFile cfg = type.getAnnotation(CsvFile.class);
        sep = (cfg != null ? cfg.separator() : Separator.COMMA).symbol();
        writeHeader = cfg == null || cfg.header();
        fields = MetadataCache.get(type).getFields();
        try {
            if (writeHeader) {
                writeHeader(out);
            }
        } catch (IOException e) {
            throw new CsvWriteException(e);
        }
    }

    public static <T> void write(Class<T> type, Collection<T> data, Path path, OpenOption... option) {
        if (data.isEmpty()) return;
        try (Output output = Factory.INSTANCE.writerOf(path, option);
             FastCsvWriter<T> writer = new FastCsvWriter<>(type, output)) {
            writer.write(data);
        } catch (Throwable e) {
            throw new CsvWriteException(e);
        }
    }

    public static <T> void write(Collection<T> data, Path path, OpenOption... option) {
        if (data.isEmpty()) return;
        Iterator<T> it = data.iterator();
        if (!it.hasNext()) return;
        T first = it.next();
        if (first == null) {
            throw new CsvWriteException("The first element of the collection = null, it is impossible to determine the type");
        }
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) first.getClass();
        write(type, data, path, option);
    }

    public void write(Collection<T> items) throws Throwable {
        for (T item : items) {
            writeRow(item, out);
        }
    }

    public void write(T data) throws Throwable {
        writeRow(data, out);
    }

    private void writeHeader(Output out) throws IOException {
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) out.append(sep);
            writeEscaped(out, fields.get(i).getHeader());
        }
        out.newLine();
    }

    private void writeRow(T dto, Output out) throws Throwable {
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) out.append(sep);
            CsvFieldMeta meta = fields.get(i);
            Object raw = meta.getGetter().invoke(dto);
            Object converted = meta.getConverter().convert(raw);
            if (converted != null) {
                if (raw instanceof Boolean) {
                    out.text().print(((Boolean) raw).booleanValue());
                } else if (raw instanceof Character) {
                    out.text().print(((Character) raw).charValue());
                } else if (raw instanceof Integer) {
                    out.text().print(((Integer) raw).intValue());
                } else if (raw instanceof Short) {
                    out.text().print(((Short) raw).shortValue());
                } else if (raw instanceof Byte) {
                    out.text().print(((Byte) raw).byteValue());
                } else if (raw instanceof Double) {
                    out.text().print((Double) raw, 2);
                } else if (raw instanceof Float) {
                    out.text().print(((Float) raw).doubleValue(), 2);
                } else {
                    writeEscaped(out, converted.toString());
                }
            }
        }
        out.newLine();
    }

    private void writeEscaped(Output out, String value) throws IOException {
        boolean needQuote = false;
        int len = value.length();

        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c == sep || c == '"' || c == '\n' || c == '\r') {
                needQuote = true;
                break;
            }
        }

        if (!needQuote) {
            out.append(value);
            return;
        }

        out.append('"');
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c == '"') {
                out.append('"');
            }
            out.append(c);
        }
        out.append('"');
    }

    @Override
    public void flush() throws IOException {
        if (out == null) return;
        out.flush();
    }

    @Override
    public void close() throws Exception {
        if (out == null) return;
        out.flush();
        out.close();
    }
}