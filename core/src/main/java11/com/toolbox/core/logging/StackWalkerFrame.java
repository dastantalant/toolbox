package com.toolbox.core.logging;

import java.util.Objects;
import java.util.Set;

public class StackWalkerFrame {
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final Set<Class<?>> INTERNAL_CLASSES = Set.of(FastLogger.class, Level.class, Entry.class);
    private final StackWalker.StackFrame value;
    public StackWalkerFrame(StackWalker.StackFrame value) {this.value = value;}
    public static StackWalkerFrame frame() {
        StackWalker.StackFrame stackFrame = WALKER.walk(frames -> Objects.requireNonNull(frames
                .filter(frame -> !INTERNAL_CLASSES.contains(frame.getDeclaringClass()))
                .findFirst()
                .orElse(null)));
        return new StackWalkerFrame(stackFrame);
    }
    public String getClassName() {return value.getClassName();}
    public int getLineNumber() {return value.getLineNumber();}
}
