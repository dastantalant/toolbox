package com.toolbox.core.logging;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class FastLogger {

    public enum Level {
        TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4);
        private final int code;

        Level(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private static final int CAPACITY = 8192;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final String PLACEHOLDER = "{}";
    private static final int WIDTH = 35;
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final Set<Class<?>> INTERNAL_CLASSES = Set.of(FastLogger.class, Level.class, Entry.class);

    private static volatile Level level = Level.INFO;
    private static final BlockingQueue<Entry> QUEUE;
    private static final Charset CHARSET;
    private static final boolean IS_ASCII;

    private static Properties prop;
    private static volatile boolean running = true;
    private static volatile long lastMillis = -1L;
    private static volatile String cachedTimestamp;

    private static final byte[][] LEVELS = new byte[Level.values().length][];
    private static final byte[] OPEN;
    private static final byte[] CLOSE;
    private static final byte[] SPACE;
    private static final byte[] NL;
    private static final byte[] EXCEPTION;
    private static final byte[] PADDING_SPACES;

    static {
        int capacity = CAPACITY;
        String cap = getProperty("logger.capacity");
        if (cap != null) {
            try {
                capacity = Integer.parseInt(cap);
                if (capacity < 128) capacity = 128;
            } catch (NumberFormatException e) {
                System.err.println("[FastLogger] Invalid capacity. Using default");
            }
        }
        QUEUE = new ArrayBlockingQueue<>(capacity);
        String loggerLevel = getProperty("logger.level");
        if (loggerLevel != null) {
            try {
                level = Level.valueOf(loggerLevel.toUpperCase().trim());
            } catch (Exception e) {
                System.err.println("[FastLogger] Invalid level. Using INFO");
            }
        }
        String encoding = getProperty("logger.console.encoding");
        Charset charset = Charset.defaultCharset();
        if (encoding != null) {
            try {
                charset = Charset.forName(encoding);
            } catch (Exception e) {
                System.err.println("[FastLogger] Invalid encoding '" + encoding + "'. Using system default");
            }
        }
        CHARSET = charset;
        IS_ASCII = StandardCharsets.US_ASCII.equals(charset) || StandardCharsets.ISO_8859_1.equals(charset);
        OPEN = "[".getBytes(CHARSET);
        CLOSE = "] ".getBytes(CHARSET);
        SPACE = " ".getBytes(CHARSET);
        NL = System.lineSeparator().getBytes(CHARSET);
        EXCEPTION = " - Exception: ".getBytes(CHARSET);
        PADDING_SPACES = " ".repeat(WIDTH).getBytes(CHARSET);
        for (Level lvl : Level.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append(lvl.name());
            if (lvl.name().length() < 5) sb.append(" ");
            LEVELS[lvl.ordinal()] = sb.toString().getBytes(CHARSET);
        }
        Thread loggerThread = new Thread(FastLogger::processLogs);
        loggerThread.setDaemon(true);
        loggerThread.setName("FastLogger-Worker");
        loggerThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            loggerThread.interrupt();
            try {
                loggerThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    public static void setLevel(Level level) {
        FastLogger.level = level;
    }

    public static void shutdown() {
        running = false;
    }

    public static boolean isTraceEnabled() {
        return level.getCode() <= Level.TRACE.getCode();
    }

    public static boolean isDebugEnabled() {
        return level.getCode() <= Level.DEBUG.getCode();
    }

    public static void trace(String message, Object... args) {
        if (isTraceEnabled()) log(Level.TRACE, message, null, args);
    }

    public static void debug(String message, Object... args) {
        if (isDebugEnabled()) log(Level.DEBUG, message, null, args);
    }

    public static void info(String message, Object... args) {
        if (level.getCode() <= Level.INFO.getCode()) log(Level.INFO, message, null, args);
    }

    public static void warn(String message, Object... args) {
        if (level.getCode() <= Level.WARN.getCode()) log(Level.WARN, message, null, args);
    }

    public static void error(Throwable t, Object... args) {
        log(Level.ERROR, t.getMessage(), t, args);
    }

    public static void error(String message, Throwable t, Object... args) {
        log(Level.ERROR, message, t, args);
    }

    public static void error(String message, Object... args) {
        log(Level.ERROR, message, null, args);
    }

    private static void log(Level level, String pattern, Throwable throwable, Object... args) {
        Entry entry = Entry.of(level, pattern, throwable, args);
        if (!QUEUE.offer(entry)) {
            System.err.println("DROP " + pattern);
        }
    }

    private static void processLogs() {
        try (OutputStream out = new BufferedOutputStream(System.out, 8192)) {
            while (running || !QUEUE.isEmpty()) {
                try {
                    Entry entry = QUEUE.poll(100, TimeUnit.MILLISECONDS);
                    if (entry == null) {
                        continue;
                    }
                    write(out, getFormattedTimestamp());
                    out.write(SPACE);
                    out.write(LEVELS[entry.level().ordinal()]);
                    out.write(SPACE);
                    String location;
                    if (entry.frame() != null) {
                        String full = entry.frame().getClassName();
                        String simple = full.substring(full.lastIndexOf('.') + 1);
                        location = "(" + simple + ".java:" + entry.frame().getLineNumber() + ")";
                    } else {
                        location = "(Unknown Source)";
                    }
                    write(out, location);
//                    int padding = WIDTH - location.length();
//                    if (padding > 0) {
//                        out.write(PADDING_SPACES, 0, padding);
//                    }
                    out.write(SPACE);
                    String message = formating(entry.pattern(), entry.args());
                    write(out, message);
                    if (entry.throwable() != null) {
                        out.write(EXCEPTION);
                        PrintStream ps = new PrintStream(out, false, CHARSET);
                        entry.throwable().printStackTrace(ps);
                        ps.flush();
                    }
                    out.write(NL);
                    if (QUEUE.isEmpty()) {
                        out.flush();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (IOException e) {
                    System.err.println("[FastLogger] Error in worker thread:");
                    e.printStackTrace();
                    break;
                }
            }
            try {
                out.flush();
            } catch (IOException e) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write(OutputStream out, String string) throws IOException {
        if (string == null) return;
        if (IS_ASCII) {
            int len = string.length();
            for (int i = 0; i < len; i++) {
                out.write((byte) string.charAt(i));
            }
        } else {
            out.write(string.getBytes(CHARSET));
        }
    }

    private static String formating(String pattern, Object... args) {
        if (pattern == null) return "";
        if (args == null || args.length == 0) return pattern;
        StringBuilder builder = new StringBuilder(pattern.length() + 50);
        int index = 0;
        int end = 0;
        int start = pattern.indexOf(PLACEHOLDER, end);
        while (start != -1) {
            builder.append(pattern, end, start);
            if (index < args.length) builder.append(args[index++]);
            else builder.append(PLACEHOLDER);
            end = start + 2;
            start = pattern.indexOf(PLACEHOLDER, end);
        }
        builder.append(pattern, end, pattern.length());
        return builder.toString();
    }

    private static String getProperty(String key) {
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) return value;
        value = System.getenv(key.replace('.', '_').toUpperCase());
        if (value != null && !value.isBlank()) return value;
        return getPropertyFromFile(key);
    }

    private static String getPropertyFromFile(String key) {
        if (prop == null) {
            prop = new Properties();
            try (InputStream input = FastLogger.class.getClassLoader().getResourceAsStream("logging.properties")) {
                if (input != null) prop.load(input);
            } catch (Exception ignored) {
            }
        }
        return prop.getProperty(key);
    }

    private static String getFormattedTimestamp() {
        long now = System.currentTimeMillis();
        if (now != lastMillis) {
            lastMillis = now;
            cachedTimestamp = LocalDateTime.now().format(FMT);
        }
        return cachedTimestamp;
    }

    private record Entry(
            Level level,
            String pattern,
            Object[] args,
            Throwable throwable,
            StackWalker.StackFrame frame
    ) {
        public static Entry of(Level level, String pattern, Throwable throwable, Object... args) {
            StackWalker.StackFrame stackFrame = WALKER.walk(frames -> frames
                    .filter(frame -> !INTERNAL_CLASSES.contains(frame.getDeclaringClass()))
                    .findFirst()
                    .orElse(null));
            return new Entry(level, pattern, args, throwable, stackFrame);
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
            return "Entry[" +
                    "level=" + level +
                    ", pattern='" + pattern + '\'' +
                    ", args=" + Arrays.toString(args) +
                    ", throwable=" + throwable +
                    ", frame=" + frame +
                    ']';
        }
    }
}
