package com.toolbox.excel.api.reflect;

import com.toolbox.excel.api.CellParser;
import com.toolbox.excel.api.RowData;
import com.toolbox.excel.api.RowMapper;
import com.toolbox.excel.api.annotation.ExcelColumn;
import com.toolbox.excel.api.context.DeserializationContext;
import com.toolbox.excel.api.FieldMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionRowMapper<T> implements RowMapper<T> {

    private final Class<T> clazz;
    private final List<FieldMeta> metas;

    public ReflectionRowMapper(Class<T> clazz, DeserializationContext ctx) {
        this.clazz = clazz;
        metas = new ArrayList<>();
        scan(clazz, ctx);
    }

    private void scan(Class<T> type, DeserializationContext ctx) {
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(ExcelColumn.class)) {
                ExcelColumn ann = field.getAnnotation(ExcelColumn.class);
                CellParser<?> parser = ctx.findParser(field.getType());
                if (parser == null) throw new IllegalStateException("No parser for " + field.getType());
                field.setAccessible(true);
                metas.add(new FieldMeta(field, ann.index(), ann.value(), ann.format(), parser));
            }
        }
    }

    @Override
    public T map(RowData row, DeserializationContext context) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (FieldMeta meta : metas) {
                String val = null;
                if (meta.index() != -1) {
                    val = row.get(meta.index());
                } else if (!meta.name().isEmpty()) {
                    val = row.get(meta.name());
                }

                if (val != null) {
                    context.setFormat(meta.format());
                    Object parsed = meta.parser().parse(val, context);
                    meta.field().set(instance, parsed);
                }
            }
            context.setFormat(null);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Mapping failed at row " + row.getRowIndex(), e);
        }
    }
}