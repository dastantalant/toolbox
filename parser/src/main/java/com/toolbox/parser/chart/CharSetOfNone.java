/*
 * @(#)CharSetOfNone.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */
package com.toolbox.parser.chart;

public final class CharSetOfNone implements CharSet {

    public CharSetOfNone() {
    }

    public boolean containsKey(char ch) {
        return false;
    }
}
