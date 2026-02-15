package com.toolbox.excel.fast.parser;

import com.toolbox.excel.api.ArrayRowData;
import com.toolbox.excel.api.RowData;
import com.toolbox.excel.fast.opc.OpcPackage;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsyncSheetParser implements AutoCloseable {
    private final XMLStreamReader reader;
    private final SharedStringsTable sst;
    private final StylesTable styles;
    private final Map<String, Integer> headerMap = new HashMap<>();

    // Reusable buffers
    private final StringBuilder textBuffer = new StringBuilder();
    private final List<String> cellValues = new ArrayList<>();

    public AsyncSheetParser(OpcPackage opc, String sheetPath, SharedStringsTable sst, StylesTable styles) throws Exception {
        this.sst = sst;
        this.styles = styles;
        this.reader = opc.createReader(opc.getEntryStream(sheetPath));
    }

    public RowData readNext() throws Exception {
        if (!reader.hasNext()) return null;

        cellValues.clear();
        int rowIndex = -1;
        int autoColIdx = 0;

        String type = null;
        String styleIdx = null;
        String cellRef = null;
        boolean insideValue = false;
        boolean insideInlineStr = false; // Для <is><t>val</t></is>

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = reader.getLocalName();
                switch (name) {
                    case "row" -> {
                        String r = reader.getAttributeValue(null, "r");
                        rowIndex = (r != null) ? Integer.parseInt(r) - 1 : -1; // -1 means auto-increment handled by caller if needed
                        autoColIdx = 0;
                    }
                    case "c" -> {
                        type = reader.getAttributeValue(null, "t");
                        styleIdx = reader.getAttributeValue(null, "s");
                        cellRef = reader.getAttributeValue(null, "r");
                        textBuffer.setLength(0);
                    }
                    case "v" -> insideValue = true;
                    case "t" -> insideValue = true; // Text inside SharedString or InlineString
                    case "is" -> insideInlineStr = true; // Inline String wrapper
                }
            }
            else if (event == XMLStreamConstants.CHARACTERS) {
                if (insideValue) textBuffer.append(reader.getText());
            }
            else if (event == XMLStreamConstants.END_ELEMENT) {
                String name = reader.getLocalName();
                switch (name) {
                    case "v", "t" -> insideValue = false;
                    case "is" -> insideInlineStr = false;
                    case "c" -> {
                        String raw = textBuffer.toString();
                        String val = resolveValue(raw, type, styleIdx);

                        int colIdx = (cellRef != null) ? getColIdx(cellRef) : autoColIdx++;
                        if (cellRef != null) autoColIdx = colIdx + 1;

                        while (cellValues.size() <= colIdx) cellValues.add(null);
                        cellValues.set(colIdx, val);
                    }
                    case "row" -> {
                        return buildRow(rowIndex);
                    }
                }
            }
        }
        return null;
    }

    private String resolveValue(String raw, String type, String styleIdx) {
        if (raw.isEmpty()) return null;

        // 1. Shared String
        if ("s".equals(type)) {
            try {
                int idx = Integer.parseInt(raw);
                return sst.get(idx);
            } catch (Exception e) { return raw; }
        }
        // 2. Boolean
        if ("b".equals(type)) return "1".equals(raw) ? "true" : "false";
        // 3. Inline String (уже в буфере) или String
        if ("str".equals(type) || "inlineStr".equals(type)) return raw;
        // 4. Number / Date
        return styles.format(raw, styleIdx); // Делегируем форматирование
    }

    private RowData buildRow(int rowIndex) {
        String[] data = cellValues.toArray(new String[0]);
        // Header detection logic
        if (headerMap.isEmpty()) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] != null) headerMap.putIfAbsent(data[i], i);
            }
        }
        return new ArrayRowData(rowIndex, data, headerMap);
    }

    private int getColIdx(String ref) {
        // Упрощенная версия без POI CellReference
        int index = 0;
        for (int i = 0; i < ref.length(); i++) {
            char c = ref.charAt(i);
            if (Character.isDigit(c)) break; // Stop at row number
            index = index * 26 + (c - 'A' + 1);
        }
        return index - 1;
    }

    @Override
    public void close() {
        try { reader.close(); } catch (Exception e) {}
    }
}