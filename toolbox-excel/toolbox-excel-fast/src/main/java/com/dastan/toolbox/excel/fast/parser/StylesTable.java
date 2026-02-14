package com.dastan.toolbox.excel.fast.parser;

import com.dastan.toolbox.excel.fast.opc.OpcPackage;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StylesTable {
    // Epoch: December 30, 1899
    private static final LocalDate EXCEL_EPOCH = LocalDate.of(1899, 12, 30);

    // Связка: Индекс стиля (s="1") -> ID формата (numFmtId="164")
    private final List<Integer> xfToFmtId = new ArrayList<>();

    // Связка: ID формата (numFmtId="164") -> Строка формата ("yyyy-mm-dd")
    private final Map<Integer, String> customFormats = new HashMap<>();

    public StylesTable(OpcPackage opc) {
        try (InputStream stream = opc.getEntryStream("xl/styles.xml")) {
            if (stream == null) return;

            XMLStreamReader reader = opc.createReader(stream);
            boolean insideCellXfs = false;

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String name = reader.getLocalName();

                    if ("numFmt".equals(name)) {
                        String id = reader.getAttributeValue(null, "numFmtId");
                        String code = reader.getAttributeValue(null, "formatCode");
                        if (id != null && code != null) {
                            customFormats.put(Integer.parseInt(id), code);
                        }
                    }
                    else if ("cellXfs".equals(name)) {
                        insideCellXfs = true;
                    }
                    else if ("xf".equals(name) && insideCellXfs) {
                        String fmtId = reader.getAttributeValue(null, "numFmtId");
                        xfToFmtId.add(fmtId != null ? Integer.parseInt(fmtId) : 0);
                    }
                }
                else if (event == XMLStreamConstants.END_ELEMENT) {
                    if ("cellXfs".equals(reader.getLocalName())) insideCellXfs = false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Styles", e);
        }
    }

    /**
     * Форматирует сырое значение ячейки согласно её стилю.
     * Если это дата -> возвращает ISO строку.
     * Если число -> возвращает число.
     */
    public String format(String rawValue, String styleIndexStr) {
        if (styleIndexStr == null || rawValue.isEmpty()) return rawValue;

        try {
            int styleIdx = Integer.parseInt(styleIndexStr);
            if (isDate(styleIdx)) {
                return parseExcelDate(Double.parseDouble(rawValue));
            }
        } catch (NumberFormatException ignored) {
            // Если что-то пошло не так, возвращаем сырое значение
        }
        return rawValue;
    }

    private boolean isDate(int styleIndex) {
        if (styleIndex < 0 || styleIndex >= xfToFmtId.size()) return false;
        int fmtId = xfToFmtId.get(styleIndex);

        // Встроенные форматы дат (14-22, 45-47)
        if ((fmtId >= 14 && fmtId <= 22) || (fmtId >= 45 && fmtId <= 47)) return true;

        // Кастомные форматы
        String code = customFormats.get(fmtId);
        return code != null && code.matches("(?i).*[dmyhs].*"); // Простая эвристика
    }

    private String parseExcelDate(double value) {
        if (value < 0) return String.valueOf(value);
        int days = (int) Math.floor(value);
        double fraction = value - days;
        long secondsInDay = Math.round(fraction * 24 * 60 * 60);

        LocalDateTime ldt = EXCEL_EPOCH.plusDays(days).atTime(LocalTime.ofSecondOfDay(secondsInDay));
        return ldt.toString(); // Возвращает ISO-8601 (yyyy-MM-ddTHH:mm:ss)
    }
}