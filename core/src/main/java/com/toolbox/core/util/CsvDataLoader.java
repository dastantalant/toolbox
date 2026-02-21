package com.toolbox.core.util;

import com.toolbox.core.logging.FastLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvDataLoader {
    private CsvDataLoader() {}
    public static Deque<Integer> load(String filePath) {
        try {
            return load(Paths.get(filePath));
        } catch (IOException ex) {
            FastLogger.error(ex);
        }
        return new LinkedList<>();
    }

    public static Deque<Integer> load(Path path) throws IOException {
        if (!Files.exists(path)) throw new FileNotFoundException("Файл не найден: " + path);
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .skip(1)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toCollection(LinkedList::new));
        }
    }
}