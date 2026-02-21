package com.toolbox.core.logging;

import java.util.Arrays;
import java.util.Objects;

public class Entry {

    private final Level level;
    private final String pattern;
    private final Object[] args;
    private final Throwable throwable;
    private final StackWalkerFrame frame;

    public Entry(Level level, String pattern, Object[] args, Throwable throwable, StackWalkerFrame frame) {
        this.level = level;
        this.pattern = pattern;
        this.args = args;
        this.throwable = throwable;
        this.frame = frame;
    }

    public static Entry of(Level level, String pattern, Throwable throwable, Object... args) {
        return new Entry(level, pattern, args, throwable, StackWalkerFrame.frame());
    }

    public Level level() {
        return level;
    }

    public String pattern() {
        return pattern;
    }

    public Object[] args() {
        return args;
    }

    public Throwable throwable() {
        return throwable;
    }

    public StackWalkerFrame frame() {
        return frame;
    }

    public String getClassName() {
        return frame == null ? "(Unknown Source)" : frame.getClassName();
    }

    public int getLineNumber() {
        return frame == null ? 0 : frame.getLineNumber();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry entry = (Entry) o;
        return level == entry.level &&
                Objects.equals(pattern, entry.pattern) &&
                Arrays.equals(args, entry.args) &&
                Objects.equals(throwable, entry.throwable) &&
                Objects.equals(frame, entry.frame);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(level, pattern, throwable, frame);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return "Entry[level=" + level +
                ", pattern='" + pattern + '\'' +
                ", args=" + Arrays.toString(args) +
                ", throwable=" + throwable +
                ", frame=" + frame + ']';
    }
}