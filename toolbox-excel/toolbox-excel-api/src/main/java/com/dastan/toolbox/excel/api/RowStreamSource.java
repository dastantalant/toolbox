package com.dastan.toolbox.excel.api;

import java.util.stream.Stream;

public interface RowStreamSource extends AutoCloseable {

    Stream<RowData> stream();

    @Override
    void close();
}