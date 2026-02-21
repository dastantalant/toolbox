package com.toolbox.io.internal;

public final class AccessorFactory {
    private AccessorFactory() {}
    public static Accessor create(){
        return new VarHandleAccessor();
    }
}
