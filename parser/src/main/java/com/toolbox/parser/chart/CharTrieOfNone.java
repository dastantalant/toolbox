/*
 * @(#)CharTrieOfNone.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */
package com.toolbox.parser.chart;

final class CharTrieOfNone implements CharTrie {
    @Override
    public int match(CharSequence str) {
        return 0;
    }

    @Override
    public int match(CharSequence str, int startIndex, int endIndex) {
        return 0;
    }

    @Override
    public int match(char[] str) {
        return 0;
    }

    @Override
    public int match(char[] str, int startIndex, int endIndex) {
        return 0;
    }

}
