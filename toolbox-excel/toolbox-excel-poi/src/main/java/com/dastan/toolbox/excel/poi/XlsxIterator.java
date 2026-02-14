package com.dastan.toolbox.excel.poi;

import com.dastan.toolbox.excel.api.RowData;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class XlsxIterator implements Iterator<RowData>, AutoCloseable {

    private final XlsxContext ctx;
    private final StaxRowParser parser;

    private RowData nextRow;
    private boolean closed = false;

    XlsxIterator(XlsxContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.ctx = ctx;
        parser = ctx.parser();
        advance();
    }

    private XlsxIterator() {
        ctx = null;
        parser = null;
        closed = true;
        nextRow = null;
    }

    public static XlsxIterator empty() {
        return new XlsxIterator();
    }

    private void advance() {
        if (closed || parser == null) return;

        try {
            nextRow = parser.parseNextRow();
            if (nextRow == null) {
                close();
            }
        } catch (Exception e) {
            close();
            throw new RuntimeException("Error during row parsing", e);
        }
    }

    @Override
    public boolean hasNext() {
        return !closed && nextRow != null;
    }

    @Override
    public RowData next() {
        if (!hasNext()) throw new NoSuchElementException();
        RowData current = nextRow;
        advance();
        return current;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        if (ctx != null) {
            try {
                if (ctx.reader() != null) ctx.reader().close();
            } catch (Exception ignored) {
            }
            try {
                if (ctx.sheetStream() != null) {
                    ctx.sheetStream().close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (ctx.opcPackage() != null) ctx.opcPackage().revert();
            } catch (Exception ignored) {
            }
        }
    }
}