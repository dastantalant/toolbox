/*
 * @(#)FormatCharSet.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */
package com.toolbox.parser.chart;

/**
 * This format set contains all Unicode format chars.
 */
public class FormatCharSet implements CharSet {
    @Override
    public boolean containsKey(char ch) {
        return Character.getType(ch) == Character.FORMAT;
    }
}
