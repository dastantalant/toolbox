package com.toolbox.excel.api;

import java.lang.reflect.Field;

public class FieldMeta {
    private Field field;
    private int index;
    private String name;
    private String format;
    private CellParser<?> parser;

    public FieldMeta(Field field, int index, String name, String format, CellParser<?> parser) {
        this.field = field;
        this.index = index;
        this.name = name;
        this.format = format;
        this.parser = parser;
    }

    public FieldMeta() {
    }

    public Field getField() {
        return field;
    }

    public Field field() {
        return field;
    }

    public FieldMeta setField(Field field) {
        this.field = field;
        return this;
    }

    public FieldMeta field(Field field) {
        this.field = field;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public int index() {
        return index;
    }

    public FieldMeta setIndex(int index) {
        this.index = index;
        return this;
    }

    public FieldMeta index(int index) {
        this.index = index;
        return this;
    }

    public String getName() {
        return name;
    }

    public String name() {
        return name;
    }

    public FieldMeta setName(String name) {
        this.name = name;
        return this;
    }

    public FieldMeta name(String name) {
        this.name = name;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public String format() {
        return format;
    }

    public FieldMeta setFormat(String format) {
        this.format = format;
        return this;
    }

    public FieldMeta format(String format) {
        this.format = format;
        return this;
    }

    public CellParser<?> getParser() {
        return parser;
    }

    public CellParser<?> parser() {
        return parser;
    }

    public FieldMeta setParser(CellParser<?> parser) {
        this.parser = parser;
        return this;
    }

    public FieldMeta parser(CellParser<?> parser) {
        this.parser = parser;
        return this;
    }
}