package com.toolbox.core.io.text;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

@Deprecated
public class FastTextReader implements Closeable {

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int EOF = -1;

    private final InputStream in;
    private final byte[] buf;
    private final int capacity;

    private int pos = 0;
    private int limit = 0;
    private boolean eof = false;

    private final StringBuilder stringBuilder = new StringBuilder(64);

    public FastTextReader(InputStream in) {
        this(in, DEFAULT_BUFFER_SIZE);
    }

    public FastTextReader(InputStream in, int bufferSize) {
        this.in = in;
        this.capacity = bufferSize;
        this.buf = new byte[bufferSize];
    }

    /**
     * Проверяет, есть ли еще данные для чтения (исключая пробельные символы в конце).
     */
    public boolean hasMore() throws IOException {
        ensureCapacity();
        return !eof || pos < limit;
    }

    /**
     * Читает int до следующего разделителя (не цифры).
     */
    public int readInt() throws IOException {
        long l = readLong();
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new NumberFormatException("Integer overflow: " + l);
        }
        return (int) l;
    }

    /**
     * Читает long до следующего разделителя (не цифры).
     * Пропускает начальные пробелы, обрабатывает знак '-'.
     */
    public long readLong() throws IOException {
        skipWhitespace();

        if (pos >= limit && eof) throw new IOException("Unexpected EOF");

        boolean negative = false;
        if (buf[pos] == '-') {
            negative = true;
            pos++;
            ensureCapacity();
        } else if (buf[pos] == '+') {
            pos++;
            ensureCapacity();
        }

        long result = 0;
        boolean hasDigits = false;

        while (true) {
            if (pos >= limit) {
                fillBuffer();
                if (pos >= limit) break; // EOF
            }

            byte b = buf[pos];
            if (b >= '0' && b <= '9') {
                result = result * 10 + (b - '0');
                hasDigits = true;
                pos++;
            } else {
                break; // Не цифра, заканчиваем чтение числа
            }
        }

        if (!hasDigits) {
            throw new NumberFormatException("No digits found");
        }

        return negative ? -result : result;
    }

    /**
     * Читает double. Оптимизировано для стандартного формата (напр. 123.456).
     * Поддерживает отрицательные числа.
     * Если встречается NaN или Infinity, обрабатывает их.
     */
    public double readDouble() throws IOException {
        skipWhitespace();

        // Быстрая проверка на спец. значения (NaN, Infinity), если начинается не с цифры/знака
        if (pos < limit) {
            byte b = buf[pos];
            if ((b == 'N' || b == 'I' || b == 'n' || b == 'i')) {
                String special = readStringUntilDelimiter();
                return Double.parseDouble(special);
            }
        }

        boolean negative = false;
        if (pos < limit && buf[pos] == '-') {
            negative = true;
            pos++;
            ensureCapacity();
        }

        long whole = 0;
        boolean hasWhole = false;

        // Читаем целую часть
        while (true) {
            if (pos >= limit) {
                fillBuffer();
                if (pos >= limit) break;
            }
            byte b = buf[pos];
            if (b >= '0' && b <= '9') {
                whole = whole * 10 + (b - '0');
                hasWhole = true;
                pos++;
            } else {
                break;
            }
        }

        double result = (double) whole;

        // Читаем дробную часть, если есть точка
        if (pos < limit && buf[pos] == '.') {
            pos++; // skip dot
            double fraction = 0.0;
            double divisor = 1.0;
            boolean hasFrac = false;

            while (true) {
                if (pos >= limit) {
                    fillBuffer();
                    if (pos >= limit) break;
                }
                byte b = buf[pos];
                if (b >= '0' && b <= '9') {
                    fraction = fraction * 10 + (b - '0');
                    divisor *= 10.0;
                    hasFrac = true;
                    pos++;
                } else {
                    break;
                }
            }
            if (hasFrac) {
                result += fraction / divisor;
            }
        }

        if (!hasWhole && result == 0.0) {
            // Возможно это было ".5" или просто мусор, если ничего не прочитали
            // Для простоты считаем валидным числом 0.0 если цифр не было,
            // но в строгом режиме здесь можно кинуть exception.
        }

        return negative ? -result : result;
    }

    /**
     * Читает boolean (true/false).
     */
    public boolean readBoolean() throws IOException {
        skipWhitespace();
        if (pos + 4 > limit) fillBuffer(); // Ensure enough bytes for "true"

        if (buf[pos] == 't') {
            // Пропускаем "true"
            pos += 4;
            return true;
        } else if (buf[pos] == 'f') {
            // Пропускаем "false"
            pos += 5;
            return false;
        }
        // Fallback or error
        throw new IOException("Expected boolean, found byte: " + (char)buf[pos]);
    }

    /**
     * Читает строку до указанного разделителя (например, запятой или \n).
     * Разделитель считывается, но не добавляется в результат.
     */
    public String readString(char delimiter) throws IOException {
        stringBuilder.setLength(0);

        while (true) {
            if (pos >= limit) {
                fillBuffer();
                if (pos >= limit) break; // EOF
            }

            byte b = buf[pos];
            if (b == delimiter) {
                pos++; // Skip delimiter
                break;
            } else if (b == '\n' || b == '\r') {
                // Если мы ищем запятую, а наткнулись на конец строки - останавливаемся
                // Но не потребляем символ конца строки, чтобы skipLine() сработал корректно
                break;
            } else {
                stringBuilder.append((char) b); // ISO-8859-1 cast
                pos++;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Внутренний метод: читает до любого не-алфавитно-цифрового символа.
     * Полезно для NaN, Infinity или простых строк.
     */
    private String readStringUntilDelimiter() throws IOException {
        stringBuilder.setLength(0);
        while (true) {
            if (pos >= limit) {
                fillBuffer();
                if (pos >= limit) break;
            }
            byte b = buf[pos];
            // Условие разделителя: пробелы, запятые, точки с запятой и переносы
            if (b <= ' ' || b == ',' || b == ';') {
                break;
            }
            stringBuilder.append((char) b);
            pos++;
        }
        return stringBuilder.toString();
    }

    /**
     * Пропускает ровно один символ (например, разделитель ',').
     * Если текущий символ отличается от ожидаемого, ничего не делает (опционально можно кидать ошибку).
     */
    public void skip(char expected) throws IOException {
        ensureCapacity();
        if (pos < limit && buf[pos] == (byte) expected) {
            pos++;
        }
    }

    /**
     * Пропускает любые символы до конца строки (\n или \r\n).
     */
    public void skipLine() throws IOException {
        while (true) {
            if (pos >= limit) {
                fillBuffer();
                if (pos >= limit) break; // EOF
            }

            byte b = buf[pos++];
            if (b == '\n') {
                break;
            } else if (b == '\r') {
                // Check for following \n
                ensureCapacity();
                if (pos < limit && buf[pos] == '\n') {
                    pos++;
                }
                break;
            }
        }
    }

    /**
     * Пропускает пробелы, табуляцию. Но НЕ пропускает переводы строк (важно для CSV).
     */
    private void skipWhitespace() throws IOException {
        while (true) {
            if (pos >= limit) {
                fillBuffer();
                if (pos >= limit) break;
            }
            byte b = buf[pos];
            if (b == ' ' || b == '\t') {
                pos++;
            } else {
                break;
            }
        }
    }

    private void ensureCapacity() throws IOException {
        if (pos >= limit) {
            fillBuffer();
        }
    }

    private void fillBuffer() throws IOException {
        if (eof) return;

        // Если у нас остались данные, которые мы не дочитали (крайний случай длинного токена),
        // это требует более сложной логики (сдвиг массива).
        // Но для CSV обычно токены меньше 8KB.
        // Здесь простая реализация: буфер читается чанками.

        limit = in.read(buf, 0, capacity);
        pos = 0;
        if (limit == -1) {
            limit = 0;
            eof = true;
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}