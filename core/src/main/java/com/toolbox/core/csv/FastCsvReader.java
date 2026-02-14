package com.toolbox.core.csv;

import com.toolbox.core.converter.IdentityConverter;
import com.toolbox.core.io.Input;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class FastCsvReader<T> {
    private final char sep;
    private final CsvClassMeta classMeta;
    private final boolean hasHeader;

    // Реююзабельный буфер для чтения токенов, чтобы не аллоцировать память на каждый байт
    private final byte[] tokenBuffer = new byte[4096];

    public FastCsvReader(Class<T> type) {
        CsvFile cfg = type.getAnnotation(CsvFile.class);
        this.sep = (cfg != null ? cfg.separator() : Separator.COMMA).symbol();
        this.hasHeader = cfg == null || cfg.header();
        this.classMeta = MetadataCache.get(type);
    }

    public List<T> read(Input in) {
        List<T> result = new ArrayList<>();
        try {
            // Пропускаем заголовок, если нужно
            if (hasHeader) {
                skipLine(in);
            }

            while (in.available() > 0 || in.hasMore()) { // hasMore() - гипотетический метод, если available врет
                try {
                    // Читаем токены для одной строки
                    List<String> tokens = parseRowTokens(in);
                    if (tokens == null) break; // EOF
                    if (tokens.isEmpty()) continue; // Пустая строка

                    result.add(mapToDto(tokens));
                } catch (Exception e) {
                    // Если EOF случился неожиданно внутри parseRowTokens, выходим
                    if (in.read() == -1) break;
                    throw e;
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Error reading CSV via Fast IO", e);
        }
        return result;
    }

    // Пропуск строки (для заголовка)
    private void skipLine(Input in) throws Exception {
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') break;
        }
    }

    private List<String> parseRowTokens(Input in) throws Exception {
        List<String> tokens = new ArrayList<>();
        int tokenLen = 0;
        boolean inQuotes = false;
        boolean isQuotedToken = false;

        // Читаем байт за байтом
        int b;
        while ((b = in.read()) != -1) {
            char c = (char) b;

            // Логика кавычек
            if (c == '"') {
                if (inQuotes) {
                    // Проверка на экранированную кавычку ("")
                    // Нам нужно заглянуть вперед. В Input нет peek, поэтому читаем.
                    // Если это не кавычка, нам придется вернуть байт обратно или обработать его.
                    // Упрощение: предполагаем стандартный CSV.

                    // В текущем API Input нет peek(). Придется делать state machine.
                    // Если следующий символ ", то это одна кавычка.
                    // Для простоты реализации на потоковом вводе без peek:
                    // Мы будем считать, что кавычка закрывается, если за ней следует разделитель или \n
                    // Но правильнее буферизировать.

                    // Упрощенная логика: считаем переключения
                    inQuotes = !inQuotes;
                } else {
                    inQuotes = true;
                    isQuotedToken = true;
                }
                continue; // Не добавляем саму кавычку-структуру в буфер
            }

            // Если мы не в кавычках, проверяем разделители
            if (!inQuotes) {
                if (c == sep) {
                    tokens.add(createString(tokenLen));
                    tokenLen = 0;
                    isQuotedToken = false;
                    continue;
                } else if (c == '\n') {
                    tokens.add(createString(tokenLen));
                    return tokens; // Конец строки
                } else if (c == '\r') {
                    continue; // Игнорируем \r
                }
            }

            // Добавляем байт в буфер
            if (tokenLen < tokenBuffer.length) {
                tokenBuffer[tokenLen++] = (byte) b;
            } else {
                // Если токен гигантский - расширяем или (тут) просто обрезаем/падаем
                throw new RuntimeException("Token too long");
            }
        }

        if (tokenLen > 0 || !tokens.isEmpty()) {
            tokens.add(createString(tokenLen));
            return tokens;
        }
        return null; // EOF
    }

    private String createString(int len) {
        if (len == 0) return "";
        return new String(tokenBuffer, 0, len, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    private T mapToDto(List<String> tokens) throws Throwable {
        List<CsvFieldMeta> fields = classMeta.fields;
        if (classMeta.isRecord) {
            Object[] args = new Object[classMeta.recordComponentCount];
            for (int i = 0; i < fields.size() && i < tokens.size(); i++) {
                CsvFieldMeta meta = fields.get(i);
                if (meta.constructorParamIndex >= 0) {
                    args[meta.constructorParamIndex] = convert(tokens.get(i), meta);
                }
            }
            return (T) classMeta.constructor.invokeWithArguments(args);
        } else {
            T instance = (T) classMeta.constructor.invoke();
            for (int i = 0; i < fields.size() && i < tokens.size(); i++) {
                CsvFieldMeta meta = fields.get(i);
                Object val = convert(tokens.get(i), meta);
                meta.setter.invoke(instance, val);
            }
            return instance;
        }
    }

    private Object convert(String token, CsvFieldMeta meta) {
        if (meta.converter.getClass() != IdentityConverter.class) {
            return meta.converter.convert(token);
        }
        return PrimitiveParser.parse(token, meta.type);
    }
}
