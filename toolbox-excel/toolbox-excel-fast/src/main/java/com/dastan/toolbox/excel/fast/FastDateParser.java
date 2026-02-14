package com.dastan.toolbox.excel.fast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class FastDateParser {
    // Excel epoch start: December 30, 1899
    private static final LocalDate EXCEL_EPOCH = LocalDate.of(1899, 12, 30);

    public static LocalDate parseDate(double value) {
        int days = (int) Math.floor(value);
        return EXCEL_EPOCH.plusDays(days);
    }


    public static LocalDateTime parseDateTime(double value) {
        if (value < 0) return EXCEL_EPOCH.atStartOfDay();

        int days = (int) Math.floor(value);
        double fraction = value - days;
        long secondsInDay = Math.round(fraction * 24 * 60 * 60);
        return EXCEL_EPOCH.plusDays(days).atTime(LocalTime.ofSecondOfDay(secondsInDay));
    }

    public static boolean isDateFormat(int formatIndex, String formatString) {
        // Стандартные встроенные форматы дат Excel (Built-in)
        // 14-22: Даты и время
        // 45-47: Времена
        if ((formatIndex >= 14 && formatIndex <= 22) ||
                (formatIndex >= 45 && formatIndex <= 47)) {
            return true;
        }

        // Если формат кастомный (обычно index > 164)
        if (formatString != null && !formatString.isEmpty()) {
            // Эвристика: ищем символы даты.
            // POI создает "yyyy-mm-dd", "m/d/yy" и т.д.
            // Регулярка проверяет наличие d, m, y, h, s и отсутствие [] (чтобы не путать с цветами [Red])
            // Это упрощенная версия.
            return formatString.matches("(?i).*[dmyhs].*");
        }

        return false;
    }
}