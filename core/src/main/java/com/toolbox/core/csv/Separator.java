package com.toolbox.core.csv;

public enum Separator {
    COMMA(','),
    SEMICOLON(';'),
    COLON(':'),
    TAB('\t'),
    PIPE('|');

    private final char symbol;

    Separator(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return symbol;
    }

    public byte byteValue() {
        return (byte) symbol;
    }
}
