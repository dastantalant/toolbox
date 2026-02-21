package com.toolbox.core.logging;

public enum Level {
    TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4);
    private final int code;
    Level(int code) {this.code = code;}
    public int getCode() {return code;}
}
