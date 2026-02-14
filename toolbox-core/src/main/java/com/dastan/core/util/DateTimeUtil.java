package com.dastan.core.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Utility for parsing ISO 8601 date and time without creating String objects.
 * <p>
 * Faster than {@link java.time.LocalDateTime#parse(CharSequence)} as it avoids
 * regex and memory allocation.
 * </p>
 * <hr>
 * Утилита для парсинга даты и времени в формате ISO 8601 без создания объектов String.
 * <p>
 * Быстрее, чем стандартный парсинг, так как не использует регулярные выражения
 * и не выделяет память в куче.
 * </p>
 */
public final class DateTimeUtil {
    private DateTimeUtil() {
    }

    /**
     * Parses an ISO 8601 string into a Unix Timestamp (Epoch Millis).
     * <p>
     * Expected format: {@code YYYY-MM-DDTHH:MM:SS.nnnnnnnnnZ}
     * <br>Example: {@code "2026-01-20T19:54:29.3928884Z"}
     * </p>
     * <p>
     * Парсит строку ISO 8601 в Unix Timestamp (миллисекунды).
     * <br>Ожидаемый формат: {@code YYYY-MM-DDTHH:MM:SS.nnnnnnnnnZ}
     * </p>
     *
     * @param seq   Input sequence / Входящая последовательность.
     * @param start Start index (inclusive) / Индекс начала (включительно).
     * @param end   End index (exclusive) / Индекс конца (исключительно).
     * @return Milliseconds since 1970-01-01 UTC / Миллисекунды с начала эпохи UTC.
     */
    public static long parseIso8601(CharSequence seq, int start, int end) {
        // Format: 2026-01-20T19:54:29.3928884Z
        int year = parseIntRange(seq, start, 4);
        int month = parseIntRange(seq, start + 5, 2); // skip '-'
        int day = parseIntRange(seq, start + 8, 2);   // skip '-'
        int hour = parseIntRange(seq, start + 11, 2); // skip 'T'
        int min = parseIntRange(seq, start + 14, 2);  // skip ':'
        int sec = parseIntRange(seq, start + 17, 2);  // skip ':'

        int nano = 0;
        // Check for fractional seconds (dot at index 19 relative to start)
        // Проверяем наличие дробных секунд (точка)
        if (start + 19 < end && seq.charAt(start + 19) == '.') {
            int i = start + 20;
            int val = 0;
            int digits = 0;
            while (i < end) {
                char c = seq.charAt(i);
                if (c < '0' || c > '9') break; // 'Z' or end
                if (digits < 9) {
                    val = val * 10 + (c - '0');
                    digits++;
                }
                i++;
            }
            // Normalize nanoseconds (e.g. .123 -> 123000000)
            // Нормализация наносекунд
            while (digits < 9) {
                val *= 10;
                digits++;
            }
            nano = val;
        }

        return LocalDateTime.of(year, month, day, hour, min, sec, nano)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli();
    }

    /**
     * Helper to parse an integer from a specific range.
     * <p>
     * Вспомогательный метод для парсинга целого числа из диапазона.
     */
    private static int parseIntRange(CharSequence seq, int offset, int len) {
        int num = 0;
        for (int i = 0; i < len; i++) {
            num = num * 10 + (seq.charAt(offset + i) - '0');
        }
        return num;
    }
}