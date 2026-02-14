package com.dastan.core.util;

/**
 * Utility class for high-performance text processing ({@link CharSequence}).
 * <p>
 * The main goal is <b>Zero-Allocation</b> (no object creation in the heap).
 * Critical for HFT (High Frequency Trading) and real-time systems.
 * </p>
 * <hr>
 * Утилитарный класс для высокопроизводительной обработки текста ({@link CharSequence}).
 * <p>
 * Основная цель — <b>Zero-Allocation</b> (отсутствие создания объектов в куче).
 * Критически важно для HFT и систем реального времени.
 * </p>
 */
public final class CharSequenceUtil {

    private CharSequenceUtil() {}

    /**
     * Precision for Fixed Point arithmetic (5 decimal places).
     * <br>Example: 1.23456 -> 123456.
     * <p>
     * Точность для арифметики с фиксированной точкой (5 знаков после запятой).
     */
    private static final int TARGET_SCALE = 5;

    /**
     * Powers of 10 for fast number scaling.
     * <p>
     * Степени десятки для быстрого масштабирования чисел.
     */
    private static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};

    /**
     * Finds the first occurrence of a substring in a sequence.
     * <p>
     * Optimized version of {@code indexOf} that works with any {@link CharSequence}
     * (e.g., StringBuilder, CharBuffer) without converting to String.
     * If the input is a String, it uses the native intrinsic method.
     * </p>
     * <p>
     * Находит первое вхождение подстроки в последовательности.
     * <br>Оптимизированная версия {@code indexOf}, работающая с любым {@link CharSequence}
     * без преобразования в String. Если передана строка, используется нативный метод.
     * </p>
     *
     * @param sequence The text to search in / Текст, в котором ищем.
     * @param needle   The substring to search for / Подстрока, которую ищем.
     * @param from     Start index / Индекс начала поиска.
     * @return Index of the first occurrence or -1 / Индекс вхождения или -1.
     */
    public static int indexOf(CharSequence sequence, String needle, int from) {
        if (sequence instanceof String str) {
            return str.indexOf(needle, from);
        }
        int sourceCount = sequence.length();
        int targetCount = needle.length();
        if (from >= sourceCount) return -1;
        if (from + targetCount > sourceCount) return -1;

        char first = needle.charAt(0);
        int max = sourceCount - targetCount;

        for (int i = from; i <= max; i++) {
            if (sequence.charAt(i) != first) {
                while (++i <= max && sequence.charAt(i) != first);
            }
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = 1; j < end && sequence.charAt(j) == needle.charAt(k); j++, k++);
                if (j == end) return i;
            }
        }
        return -1;
    }

    /**
     * Parses a {@code long} value from a sequence.
     * <p>
     * Skips leading whitespace. Handles negative numbers.
     * Stops at the first non-digit character.
     * </p>
     * <p>
     * Парсит значение {@code long} из последовательности.
     * <br>Пропускает начальные пробелы. Обрабатывает отрицательные числа.
     * Останавливается на первом нецифровом символе.
     * </p>
     *
     * @param seq   Input sequence / Входящая последовательность.
     * @param start Start index / Индекс начала.
     * @return Parsed number / Распаршенное число.
     */
    public static long parseSimpleLong(CharSequence seq, int start) {
        long res = 0;
        int i = start;
        while (i < seq.length() && Character.isWhitespace(seq.charAt(i))) i++;

        boolean negative = false;
        if (i < seq.length() && seq.charAt(i) == '-') {
            negative = true;
            i++;
        }

        for (; i < seq.length(); i++) {
            char c = seq.charAt(i);
            if (c < '0' || c > '9') break;
            res = res * 10 + (c - '0');
        }
        return negative ? -res : res;
    }

    /**
     * Parses a floating-point number from JSON into a Fixed Point {@code int}.
     * <p>
     * Multiplier: <b>100,000</b> (5 decimal places).
     * <br>Examples:
     * <ul>
     *     <li>"5.39401" -> 539401</li>
     *     <li>"5.2" -> 520000 (padded with zeros)</li>
     * </ul>
     * Excess decimal places are truncated.
     * </p>
     * <p>
     * Парсит число с плавающей точкой из JSON в {@code int} с фиксированной точкой.
     * <br>Множитель: <b>100,000</b> (5 знаков).
     * <br>Если знаков меньше, число дополняется нулями. Если больше — обрезается.
     * </p>
     *
     * @param seq   Input sequence / Входящая последовательность.
     * @param start Start index / Индекс начала числа.
     * @return Scaled integer / Масштабированное целое число.
     */
    public static int parseFixedPointInt(CharSequence seq, int start) {
        int result = 0;
        int i = start;
        while (i < seq.length() && Character.isWhitespace(seq.charAt(i))) i++;

        // Integer part / Целая часть
        while (i < seq.length()) {
            char c = seq.charAt(i);
            if (c == '.') {
                i++;
                break;
            }
            if (c < '0' || c > '9') break;
            result = result * 10 + (c - '0');
            i++;
        }

        // Fractional part / Дробная часть
        int decimalsCount = 0;
        while (i < seq.length()) {
            char c = seq.charAt(i);
            if (c < '0' || c > '9') break;
            if (decimalsCount < TARGET_SCALE) {
                result = result * 10 + (c - '0');
                decimalsCount++;
            }
            i++;
        }
        // Padding / Дополнение нулями
        if (decimalsCount < TARGET_SCALE) {
            result *= POWERS_OF_10[TARGET_SCALE - decimalsCount];
        }
        return result;
    }

    /**
     * Finds the boundaries of a string value inside JSON quotes.
     * <p>
     * Used to extract string content without creating a substring object immediately.
     * </p>
     * <p>
     * Находит границы строкового значения внутри кавычек JSON.
     * <br>Используется для работы с содержимым строки без немедленного создания объекта String.
     * </p>
     *
     * @param seq         JSON sequence / JSON последовательность.
     * @param startSearch Index to start searching for the opening quote / Откуда искать открывающую кавычку.
     * @return A packed {@code long} containing two {@code int} indices:
     *         <ul>
     *           <li><b>High 32 bits:</b> Start index (after opening quote).</li>
     *           <li><b>Low 32 bits:</b> End index (before closing quote).</li>
     *         </ul>
     *         Returns -1 if not found.
     *         <br>
     *         Упакованный long: старшие 32 бита - начало, младшие 32 бита - конец.
     */
    public static long findStringBounds(CharSequence seq, int startSearch) {
        int len = seq.length();
        int i = startSearch;
        while (i < len && seq.charAt(i) != '"') i++;
        if (i >= len) return -1;
        int start = ++i;
        while (i < len && seq.charAt(i) != '"') i++;
        if (i >= len) return -1;
        return ((long) start << 32) | (long) i & 0xFFFFFFFFL;
    }
}