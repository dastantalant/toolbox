package com.toolbox.excel.test;

import com.toolbox.excel.api.ExcelMapper;
import com.toolbox.excel.fast.FastExcelSource;
import com.toolbox.excel.poi.PoiXlsxSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExcelBenchmarkTest {

    // Путь к файлу
    private static final String FILE_PATH = "C:\\Users\\dastan\\home\\github\\toolbox\\million_rows_test.xlsx";

    // Количество строк для теста.
    // Поставьте 1_000_000 для реального нагрузочного теста.
    private static final int ROW_COUNT = 1_000_000;

    private static File benchmarkFile;

    @BeforeAll
    static void setup() throws IOException {
        benchmarkFile = new File(FILE_PATH);
        if (!benchmarkFile.exists()) {
            System.out.println("Generating benchmark file with " + ROW_COUNT + " rows...");
            LargeFileGenerator.generate(FILE_PATH, ROW_COUNT);
        } else {
            System.out.println("Using existing benchmark file.");
        }
        System.gc();
    }

    @Test
    @Order(1)
    @DisplayName("POI XlsxSource (Standard Implementation)")
    void testPoiSource() {
        System.out.println("\n--- Starting POI Source Benchmark ---");
        long start = System.currentTimeMillis();
        long startMem = getUsedMemory();

        ExcelMapper mapper = new ExcelMapper();
        AtomicInteger count = new AtomicInteger();

        try (PoiXlsxSource source = new PoiXlsxSource(benchmarkFile)) {
            mapper.read(source, TestData.class, (item, row) -> {
                count.incrementAndGet();
                if (item.getId() % 50000 == 0) {
                    // System.out.println("Read row: " + item.getId());
                }
            });
        }

        long end = System.currentTimeMillis();
        long endMem = getUsedMemory();

        printResults("POI Source", count.get(), end - start, startMem, endMem);
    }

    @Test
    @Order(2)
    @DisplayName("Fast XlsxSource (Aalto XML Implementation)")
    void testFastSource() {
        System.out.println("\n--- Starting Fast Source Benchmark ---");
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        long start = System.currentTimeMillis();
        long startMem = getUsedMemory();

        ExcelMapper mapper = new ExcelMapper();
        AtomicInteger count = new AtomicInteger();

        try (FastExcelSource source = new FastExcelSource(benchmarkFile)) {
            mapper.read(source, TestData.class, (item, row) -> {
                count.incrementAndGet();
                if (item.getId() % 50000 == 0) {
                    // dummy work
                }
            });
        }

        long end = System.currentTimeMillis();
        long endMem = getUsedMemory();

        printResults("Fast Source", count.get(), end - start, startMem, endMem);
    }

    private void printResults(String name, int rows, long timeMs, long startMem, long endMem) {
        System.out.println("Result [" + name + "]:");
        System.out.println("  Rows read: " + rows);
        System.out.println("  Time:      " + timeMs + " ms (" + (timeMs / 1000.0) + " s)");
        System.out.println("  Speed:     " + (rows / (timeMs / 1000.0)) + " rows/sec");
        // Память примерная, так как GC работает непредсказуемо
        // System.out.println("  Mem Delta: ~" + ((endMem - startMem) / 1024 / 1024) + " MB");
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}