/*
 * @(#)ByteSetOfNone.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */
package com.toolbox.parser.bytet;

final class ByteSetOfNone implements ByteSet {

    ByteSetOfNone() {
    }

    public boolean containsKey(byte b) {
        return false;
    }
}
