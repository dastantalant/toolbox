package com.dastan.toolbox.excel.poi;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class CellValueResolver {
    private final StringPool stringPool;
    private final ReadOnlySharedStringsTable stringsTable;
    private final StylesTable stylesTable;
    private final DataFormatter dataFormatter;

    public CellValueResolver(ReadOnlySharedStringsTable stringsTable, StylesTable stylesTable) {
        stringPool = new StringPool();
        this.stringsTable = stringsTable;
        this.stylesTable = stylesTable;
        this.dataFormatter = new DataFormatter();
    }
    public String resolve(String rawValue, String type, String styleIndexStr) {
        String result = resolveInternal(rawValue, type, styleIndexStr);
        return stringPool.intern(result);
    }

    private String resolveInternal(String rawValue, String type, String styleIndexStr) {
        if (rawValue == null || rawValue.isEmpty()) return null;

        switch (type) {
            case "s" -> {
                try {
                    int idx = Integer.parseInt(rawValue);
                    return stringsTable.getItemAt(idx).getString();
                } catch (Exception e) {
                    return rawValue;
                }
            }
            case "b" -> {
                return "1".equals(rawValue) ? "true" : "false";
            }
            case "e" -> {
                return "ERROR:" + rawValue;
            }

            case "inlineStr" -> {
                return new XSSFRichTextString(rawValue).toString();
            }

            case null, default -> {
                if (styleIndexStr != null) {
                    try {
                        int styleIndex = Integer.parseInt(styleIndexStr);
                        XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
                        double doubleVal = Double.parseDouble(rawValue);
                        return dataFormatter.formatRawCellContents(doubleVal, style.getDataFormat(), style.getDataFormatString());
                    } catch (NumberFormatException ignored) {
                    }
                }
                return rawValue;
            }
        }
    }
}