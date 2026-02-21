package com.toolbox.core.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class ProjectDump {
    private static final int BUF_SIZE = 64 * 1024;
    private static final int LINE_BUF_SIZE = 4096;
    private static final long MAX_FILE_SIZE = 1_048_576;
    private static final byte SLASH = '/';
    private static final byte STAR = '*';
    private static final byte[] CLASS = new byte[256];

    static {
        CLASS['/'] = 1;
        CLASS['*'] = 2;
        CLASS['\n'] = 3;
    }

    private enum State {
        NORMAL, LINE_COMMENT, BLOCK_COMMENT
    }

    private final Set<String> includePackages = new HashSet<>();
    private final Set<String> excludePackages = new HashSet<>();
    private final Set<String> includeClasses = new HashSet<>();
    private final Set<String> excludeClasses = new HashSet<>();

    private Path root = findProjectRoot(Paths.get(".").toAbsolutePath());
    private Path output = Paths.get("project.md");
    private long tokenCounter;
    private boolean debug = false;
    private boolean linePackage = false;
    private boolean lineImport = false;

    private ProjectDump() {
    }

    private ProjectDump(Package target) {
        include(target);
    }

    private ProjectDump(Package... packages) {
        include(packages);
    }

    private ProjectDump(Class<?>... classes) {
        include(classes);
    }

    private ProjectDump(Package aPackage, Class<?>... classes) {
        include(aPackage);
        include(classes);
    }

    public static ProjectDump create() {
        return new ProjectDump();
    }

    public static ProjectDump of(Package target) {
        return new ProjectDump(target);
    }

    public static ProjectDump of(Class<?>... classes) {
        return new ProjectDump(classes);
    }

    public static ProjectDump of(Package aPackage, Class<?>... classes) {
        return new ProjectDump(aPackage, classes);
    }

    public ProjectDump debug() {
        debug = true;
        return this;
    }

    public ProjectDump linePackage(boolean linePackage) {
        this.linePackage = linePackage;
        return this;
    }

    public ProjectDump lineImport(boolean lineImport) {
        this.lineImport = lineImport;
        return this;
    }

    public ProjectDump root(Path path) {
        this.root = path.toAbsolutePath();
        return this;
    }

    public ProjectDump output(Path path) {
        this.output = path;
        return this;
    }

    public ProjectDump include(Package... packages) {
        for (Package pkg : packages) includePackages.add(pkg.getName());
        return this;
    }

    public ProjectDump exclude(Package... packages) {
        for (Package pkg : packages) excludePackages.add(pkg.getName());
        return this;
    }

    public ProjectDump include(Class<?>... classes) {
        for (Class<?> clazz : classes) includeClasses.add(normalizeClass(clazz.getSimpleName()));
        return this;
    }

    public ProjectDump exclude(Class<?>... classes) {
        for (Class<?> clazz : classes) excludeClasses.add(normalizeClass(clazz.getSimpleName()));
        return this;
    }

    public void write() throws IOException {
        if (debug) {
            System.out.println("ROOT           : " + root);
            System.out.println("OUTPUT         : " + output);
            System.out.println("INCLUDE PKG    : " + includePackages);
            System.out.println("EXCLUDE PKG    : " + excludePackages);
            System.out.println("INCLUDE CLASS  : " + includeClasses);
            System.out.println("EXCLUDE CLASS  : " + excludeClasses);
            System.out.println("--------------------------------------------------");
        }
        List<Path> files = scanFiles();
        try (BufferedWriter w = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writeHeader(w, files.size());
            for (Path f : files) {
                writeFileFast(w, f);
            }
        }
        System.out.println("Files  : " + files.size());
        System.out.println("Tokens : " + tokenCounter);
    }

    private List<Path> scanFiles() throws IOException {
        List<Path> result = new ArrayList<>(256);
        try (Stream<Path> walk = Files.walk(root)) {
            for (Path p : (Iterable<Path>) walk::iterator) {
                Path absPath = p.toAbsolutePath();

                if (!Files.isRegularFile(absPath)) continue;
                if (!absPath.toString().endsWith(".java")) continue;
                if (!notTooLarge(absPath)) continue;

                String clsName = normalizeClass(absPath.getFileName().toString());
                if (excludeClasses.contains(clsName)) continue;
                if (isPackageExcluded(absPath)) continue;
                boolean explicitlyIncludedClass = includeClasses.contains(clsName);
                boolean packageOk = isPackageIncluded(absPath);
                if (!explicitlyIncludedClass && !packageOk) {
                    continue;
                }
                if (debug) System.out.println("ADD: " + absPath);
                result.add(absPath);
            }
        }
        result.sort(Comparator.naturalOrder());
        return result;
    }

    private boolean isPackageExcluded(Path file) {
        String pkg = extractPackage(file);
        if (pkg.isEmpty()) return false;
        for (String ex : excludePackages) {
            if (pkg.startsWith(ex)) return true;
        }
        return false;
    }

    private boolean isPackageIncluded(Path file) {
        String pkg = extractPackage(file);
        if (pkg.isEmpty()) return false;
        if (includePackages.isEmpty()) return true;
        for (String in : includePackages) {
            if (pkg.startsWith(in)) return true;
        }
        return false;
    }

    private boolean notTooLarge(Path p) {
        try {
            return Files.size(p) <= MAX_FILE_SIZE;
        } catch (IOException e) {
            return false;
        }
    }

    private static String normalizeClass(String name) {
        return name.endsWith(".java") ? name : name + ".java";
    }

    private String extractPackage(Path file) {
        String path = file.toAbsolutePath().toString().replace('\\', '/');
        String marker = "/src/main/java/";
        int idx = path.lastIndexOf(marker);
        if (idx != -1) {
            String pkgPath = path.substring(idx + marker.length());
            int lastSlash = pkgPath.lastIndexOf('/');
            if (lastSlash != -1) {
                return pkgPath.substring(0, lastSlash).replace('/', '.');
            }
            return "";
        }
        marker = "/src/";
        idx = path.lastIndexOf(marker);
        if (idx != -1) {
            String pkgPath = path.substring(idx + marker.length());
            int lastSlash = pkgPath.lastIndexOf('/');
            if (lastSlash != -1) {
                return pkgPath.substring(0, lastSlash).replace('/', '.');
            }
        }
        return "";
    }

    private void writeFileFast(BufferedWriter w, Path file) throws IOException {
        String relPath = extractRelativePathForDisplay(file);
        w.write("## " + relPath + "\n\n```java\n");
        try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
            ByteBuffer buf = ByteBuffer.allocateDirect(BUF_SIZE);
            char[] lineBuf = new char[LINE_BUF_SIZE];
            int lineLen = 0;
            int prev = -1;
            State state = State.NORMAL;
            while (ch.read(buf) != -1) {
                buf.flip();
                while (buf.hasRemaining()) {
                    int b = buf.get() & 0xFF;
                    byte cls = CLASS[b];
                    switch (state) {
                        case NORMAL : {
                            if (prev == SLASH && b == SLASH) {
                                lineLen--;
                                state = State.LINE_COMMENT;
                            } else if (prev == SLASH && b == STAR) {
                                lineLen--;
                                state = State.BLOCK_COMMENT;
                            } else if (cls == 3) {
                                flushLine(w, lineBuf, lineLen);
                                lineLen = 0;
                            } else {
                                lineBuf[lineLen++] = (char) b;
                            }
                            break;
                        }
                        case LINE_COMMENT : {
                            if (cls == 3) {
                                state = State.NORMAL;
                                flushLine(w, lineBuf, lineLen);
                                lineLen = 0;
                            }
                            break;
                        }
                        case BLOCK_COMMENT : {
                            if (prev == STAR && b == SLASH) {
                                state = State.NORMAL;
                            }
                            break;
                        }
                    }
                    prev = b;
                }
                buf.clear();
            }
            flushLine(w, lineBuf, lineLen);
        }
        w.write("```\n\n");
    }

    private void flushLine(BufferedWriter w, char[] buf, int len) {
        if (len == 0) return;
        int start = 0;
        while (start < len && buf[start] <= ' ') start++;
        if (start == len) return;
        if (!linePackage && startsWithKeyword(buf, start, len, "package")) return;
        if (!lineImport && startsWithKeyword(buf, start, len, "import")) return;
        int end = len - 1;
        while (end > start && buf[end] <= ' ') end--;
        int outLen = end - start + 1;
        try {
            w.write(buf, start, outLen);
            w.newLine();
            tokenCounter += outLen / 4 + 1;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean startsWithKeyword(char[] buf, int start, int totalLen, String keyword) {
        int kLen = keyword.length();
        if (start + kLen > totalLen) return false;
        for (int i = 0; i < kLen; i++) {
            if (buf[start + i] != keyword.charAt(i)) return false;
        }
        if (start + kLen < totalLen) {
            char next = buf[start + kLen];
            return next <= ' ' || next == ';';
        }
        return true;
    }

    private String extractRelativePathForDisplay(Path file) {
        String path = file.toAbsolutePath().toString().replace('\\', '/');
        int idx = path.lastIndexOf("/src/main/java/");
        if (idx != -1) {
            return path.substring(idx + "/src/main/java/".length());
        }
        return root.relativize(file).toString().replace('\\', '/');
    }

    private void writeHeader(BufferedWriter w, int count) throws IOException {
        w.write("# Project Dump\n\n");
        w.write("- Root: " + root + "\n");
        w.write("- Files: " + count + "\n");
        w.write("- Generated: " + LocalDateTime.now() + "\n\n");
    }

    private static Path findProjectRoot(Path path) {
        Path cur = path.toAbsolutePath();
        while (cur != null) {
            if (Files.exists(cur.resolve("pom.xml")) ||
                    Files.exists(cur.resolve(".git")) ||
                    Files.exists(cur.resolve("build.gradle"))) {
                return cur;
            }
            cur = cur.getParent();
        }
        return path.toAbsolutePath();
    }
}
