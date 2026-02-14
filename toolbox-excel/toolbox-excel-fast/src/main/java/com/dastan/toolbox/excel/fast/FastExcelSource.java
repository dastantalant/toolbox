package com.dastan.toolbox.excel.fast;

import com.dastan.toolbox.excel.api.RowData;
import com.dastan.toolbox.excel.api.RowStreamSource;
import com.dastan.toolbox.excel.fast.opc.OpcPackage;
import com.dastan.toolbox.excel.fast.opc.SheetDescriptor;
import com.dastan.toolbox.excel.fast.parser.AsyncSheetParser;
import com.dastan.toolbox.excel.fast.parser.SharedStringsTable;
import com.dastan.toolbox.excel.fast.parser.StylesTable;

import java.io.File;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FastExcelSource implements RowStreamSource {
    private final File file;
    private OpcPackage opc;
    private AsyncSheetParser parser;
    private String sheetName;

    public FastExcelSource(File file) {
        this.file = file;
    }

    public FastExcelSource sheet(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    @Override
    public Stream<RowData> stream() {
        try {
            this.opc = new OpcPackage(file);

            // 1. Загрузка метаданных (Shared Strings & Styles)
            SharedStringsTable sst = loadSST(opc);
            StylesTable styles = loadStyles(opc);

            // 2. Поиск листа
            List<SheetDescriptor> sheets = opc.getSheets();
            SheetDescriptor targetSheet = findSheet(sheets, sheetName);

            if (targetSheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + (sheetName == null ? "first sheet" : sheetName));
            }

            // 3. Создание асинхронного парсера листа
            this.parser = new AsyncSheetParser(opc, targetSheet.zipPath(), sst, styles);

            // 4. Создание стрима
            return StreamSupport.stream(new Spliterators.AbstractSpliterator<RowData>(Long.MAX_VALUE, 0) {
                @Override
                public boolean tryAdvance(java.util.function.Consumer<? super RowData> action) {
                    try {
                        RowData row = parser.readNext();
                        if (row == null) return false;
                        action.accept(row);
                        return true;
                    } catch (Exception e) {
                        throw new RuntimeException("Error reading row", e);
                    }
                }
            }, false).onClose(this::close);

        } catch (Exception e) {
            close(); // Безопасное закрытие при ошибке открытия
            throw new RuntimeException("Failed to open Excel file", e);
        }
    }

    private SharedStringsTable loadSST(OpcPackage opc) {
        return new SharedStringsTable.InMemory(opc);
    }

    private StylesTable loadStyles(OpcPackage opc) {
        return new StylesTable(opc);
    }

    private SheetDescriptor findSheet(List<SheetDescriptor> sheets, String name) {
        if (sheets.isEmpty()) return null;
        if (name == null) return sheets.get(0);
        return sheets.stream()
                .filter(s -> s.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void close() {
        if (parser != null) parser.close();
        try { if (opc != null) opc.close(); } catch (Exception ignored) {}
    }
}