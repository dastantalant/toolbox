package com.toolbox.io.util.natiwe;

final class NativeFactory {
    private NativeFactory() {}
    static Native create() {
        return new VarHandleNative();
    }
}
