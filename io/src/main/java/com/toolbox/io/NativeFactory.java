package com.toolbox.io;

final class NativeFactory {
    private NativeFactory() {}
    static Native create() {
        return new NativeBase();
    }
}
