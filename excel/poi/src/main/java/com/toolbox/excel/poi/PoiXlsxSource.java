package com.toolbox.excel.poi;

import com.toolbox.excel.api.RowData;
import com.toolbox.excel.api.RowStreamSource;

import java.io.File;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;

public class PoiXlsxSource implements RowStreamSource {
    private final File file;
    private final XlsxSourceFactory factory;
    private XlsxIterator iterator;

    public PoiXlsxSource(File file) {
        this.file = file;
        factory = new XlsxSourceFactory();
    }

    @Override
    public Stream<RowData> stream() {
        iterator = factory.createIterator(file);
        Spliterator<RowData> split = Spliterators.spliteratorUnknownSize(iterator,ORDERED | NONNULL | IMMUTABLE);
        return StreamSupport.stream(split, false)
                .onClose(this::close);
    }

    @Override
    public void close() {
        if (iterator != null) iterator.close();
    }
}