/*
 * @(#)ByteTrieOfNone.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */
package com.toolbox.parser.bytet;

final class ByteTrieOfNone implements ByteTrie {


    @Override
    public int match(byte[] str) {
        return 0;
    }

    @Override
    public int match(byte[] str, int startIndex, int endIndex) {
        return 0;
    }
}
