package com.toolbox.excel.api.parser;

import com.toolbox.excel.api.CellParser;
import com.toolbox.excel.api.context.DeserializationContext;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Map;

public final class DefaultParsers {

    private DefaultParsers() {}

    public static void registerAll(Map<Class<?>, CellParser<?>> registry) {
        registry.put(boolean.class, new BoolPrim());
        registry.put(int.class, new IntPrim());
        registry.put(long.class, new LongPrim());
        registry.put(double.class, new DoublePrim());
        registry.put(float.class, new FloatPrim());

        registry.put(Boolean.class, new BoolWrap());
        registry.put(Integer.class, new IntWrap());
        registry.put(Long.class, new LongWrap());
        registry.put(Double.class, new DoubleWrap());
        registry.put(Float.class, new FloatWrap());

        registry.put(String.class, (CellParser<String>) (v, c) -> v);
        registry.put(BigDecimal.class, new BigDecParser());

        registry.put(LocalDate.class, new LDateParser());
        registry.put(LocalDateTime.class, new LDateTimeParser());
        registry.put(Date.class, new JavaDateParser());
    }

    private static boolean empty(String value) {
        return value == null || value.isBlank();
    }

    private static double safeDouble(String value) {
        if (empty(value)) return 0.0;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    static class BoolPrim implements CellParser<Boolean> {
        public Boolean parse(String value, DeserializationContext context) {
            if (empty(value)) return false;
            String v = value.trim().toLowerCase();
            return "true".equals(v) || "1".equals(v) || "yes".equals(v);
        }
    }

    static class BoolWrap implements CellParser<Boolean> {
        public Boolean parse(String value, DeserializationContext context) {
            if (empty(value)) return null;
            String v = value.trim().toLowerCase();
            return "true".equals(v) || "1".equals(v) || "yes".equals(v);
        }
    }

    static class IntPrim implements CellParser<Integer> {
        public Integer parse(String value, DeserializationContext context) { return (int) safeDouble(value); }
    }
    static class IntWrap implements CellParser<Integer> {
        public Integer parse(String value, DeserializationContext context) { return empty(value) ? null : (int) safeDouble(value); }
    }
    static class LongPrim implements CellParser<Long> {
        public Long parse(String value, DeserializationContext context) { return (long) safeDouble(value); }
    }
    static class LongWrap implements CellParser<Long> {
        public Long parse(String value, DeserializationContext context) { return empty(value) ? null : (long) safeDouble(value); }
    }
    static class DoublePrim implements CellParser<Double> {
        public Double parse(String value, DeserializationContext context) { return safeDouble(value); }
    }
    static class DoubleWrap implements CellParser<Double> {
        public Double parse(String value, DeserializationContext context) { return empty(value) ? null : safeDouble(value); }
    }
    static class FloatPrim implements CellParser<Float> {
        public Float parse(String value, DeserializationContext context) { return (float) safeDouble(value); }
    }
    static class FloatWrap implements CellParser<Float> {
        public Float parse(String value, DeserializationContext context) { return empty(value) ? null : (float) safeDouble(value); }
    }
    static class BigDecParser implements CellParser<BigDecimal> {
        public BigDecimal parse(String value, DeserializationContext context) { return empty(value) ? null : new BigDecimal(value.trim()); }
    }

    private static DateTimeFormatter getDtf(DeserializationContext ctx, DateTimeFormatter def) {
        String format = ctx.getFormat();
        return format != null ? DateTimeFormatter.ofPattern(format, ctx.getLocale()) : def;
    }

    static class LDateParser implements CellParser<LocalDate> {
        public LocalDate parse(String value, DeserializationContext context) {
            if (empty(value)) return null;
            String v = value.trim();

            // "2020-01-01T00:00" or "2020-01-01 12:00"
            int tIndex = v.indexOf('T');
            if (tIndex > 0) v = v.substring(0, tIndex);

            int sIndex = v.indexOf(' ');
            if (sIndex > 0) v = v.substring(0, sIndex);

            return LocalDate.parse(v, getDtf(context, DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    static class LDateTimeParser implements CellParser<LocalDateTime> {
        private static final DateTimeFormatter FALLBACK_DTF = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd")
                .optionalStart().appendLiteral(' ').optionalEnd()
                .optionalStart().appendLiteral('T').optionalEnd()
                .appendValue(ChronoField.HOUR_OF_DAY)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE)
                .optionalEnd()
                .toFormatter();

        public LocalDateTime parse(String value, DeserializationContext context) {
            if (empty(value)) return null;
            String v = value.trim();
            if (context.getFormat() != null) {
                return LocalDateTime.parse(v, DateTimeFormatter.ofPattern(context.getFormat(), context.getLocale()));
            }

            try {
                return LocalDateTime.parse(v, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception ignored) {
                return LocalDateTime.parse(v, FALLBACK_DTF);
            }
        }
    }

    static class JavaDateParser implements CellParser<Date> {
        public Date parse(String value, DeserializationContext context) {
            if (empty(value)) return null;
            String format = context.getFormat();
            String v = value.replace("T", " ");
            SimpleDateFormat sdf = new SimpleDateFormat(
                    format != null ? format : "yyyy-MM-dd HH:mm:ss",
                    context.getLocale()
            );
            try {
                return sdf.parse(v);
            } catch (ParseException e) {
                if (format == null && v.length() > 10) {
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd").parse(v.substring(0, 10));
                    } catch (ParseException ignored) {}
                }
                return null;
            }
        }
    }
}