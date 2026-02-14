/*
 * @(#)ConsecutiveByteDigitSet.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */
package com.toolbox.core.parser.chr;

final class ConsecutiveCharDigitSet implements CharDigitSet {
    private final char zeroDigit;

    public ConsecutiveCharDigitSet(char zeroDigit) {
        this.zeroDigit = zeroDigit;
    }

    @Override
    public int toDigit(char ch) {
        return (char) (ch - zeroDigit);
    }
}
