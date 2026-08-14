package com.toolbox.core.logging;

import java.util.Objects;
import java.util.Set;

class StackWalkerFrame {
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final Set<Class<?>> INTERNAL_CLASSES = Set.of(FastLogger.class, Level.class, Entry.class);
    private final StackWalker.StackFrame value;

    StackWalkerFrame(StackWalker.StackFrame value) {
        this.value = value;
    }

    static StackWalkerFrame frame() {
        StackWalker.StackFrame stackFrame = WALKER.walk(frames -> Objects.requireNonNull(frames
                .filter(frame -> !INTERNAL_CLASSES.contains(frame.getDeclaringClass()))
                .findFirst()
                .orElse(null)));
        return new StackWalkerFrame(stackFrame);
    }

    String getClassName() {
        return value.getClassName();
    }

    int getLineNumber() {
        return value.getLineNumber();
    }
}
