package com.toolbox.io;

public final class AccessorFactory {
    private AccessorFactory() {}
    public static Accessor create(){
        return new VarHandleAccessor();
    }
}
