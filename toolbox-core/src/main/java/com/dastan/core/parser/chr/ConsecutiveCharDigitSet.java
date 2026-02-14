package com.dastan.core.parser.chr;

public final class ConsecutiveCharDigitSet implements CharDigitSet {
    private final char zeroDigit;

    public ConsecutiveCharDigitSet(char zeroDigit) {
        this.zeroDigit = zeroDigit;
    }

    @Override
    public int toDigit(char ch) {
        return (char) (ch - zeroDigit);
    }
}