package com.toolbox.core.logging;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class StackWalkerFrame {
    private static final Set<String> INTERNAL_CLASSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            FastLogger.class.getName(), Level.class.getName(), Entry.class.getName())));
    private final StackTraceElement value;

    StackWalkerFrame(StackTraceElement value) {
        this.value = value;
    }

    static StackWalkerFrame frame() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stackTrace.length; i++)
            if (!INTERNAL_CLASSES.contains(stackTrace[i].getClassName()))
                return new StackWalkerFrame(stackTrace[i]);
        return new StackWalkerFrame(null);
    }

    String getClassName() {
        return value == null ? "(Unknown Source)" : value.getClassName();
    }

    int getLineNumber() {
        return value == null ? 0 : value.getLineNumber();
    }
}