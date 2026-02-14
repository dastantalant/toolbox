package com.toolbox.core.io.text;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Deprecated
public class FastTextWriterTest {

    // Количество итераций для теста производительности
    private static final int ITERATIONS = 5_000_000;

    public static void main(String[] args) throws IOException {
        System.out.println("=== 1. Проверка корректности (Functional Test) ===");
        testCorrectness();
        System.out.println("PASSED: Данные записываются корректно.\n");

        System.out.println("=== 2. Тест производительности (Benchmark) ===");
        System.out.println("Simulating CSV writing of " + ITERATIONS + " rows...");
        System.out.println("---------------------------------------------------");

        // Прогрев JIT (Warmup) - чтобы Java скомпилировала код перед замером
        System.out.print("Warming up JVM... ");
        runStandardScenario(new NullOutputStream(), 100_000);
        runFastScenario(new NullOutputStream(), 100_000);
        System.out.println("Done.");

        // --- Замер стандартного подхода ---
        System.gc(); // Просим сборщик мусора очистить память перед стартом
        long startStd = System.nanoTime();
        long bytesStd = runStandardScenario(new NullOutputStream(), ITERATIONS);
        long timeStd = System.nanoTime() - startStd;

        // --- Замер быстрого подхода ---
        System.gc();
        long startFast = System.nanoTime();
        long bytesFast = runFastScenario(new NullOutputStream(), ITERATIONS);
        long timeFast = System.nanoTime() - startFast;

        // --- Результаты ---
        printResults("Standard (BufferedWriter + toString)", timeStd, bytesStd);
        printResults("FastTextWriter (Zero-Allocation)", timeFast, bytesFast);

        double speedup = (double) timeStd / timeFast;
        System.out.printf("\n>>> FastTextWriter is %.2fx FASTER <<<\n", speedup);
    }

    /**
     * Проверяет, что то, что мы пишем, совпадает с ожидаемой строкой.
     */
    private static void testCorrectness() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FastTextWriter writer = new FastTextWriter(baos)) {
            writer.append("Round").append(';').append(100).append(';')
                    .append(1234567890123L).append(';') // long
                    .append(-55).append(';')            // negative int
                    .append(123.4567, 2).append(';')    // double precision 2
                    .append(0.999, 2).append(';')       // rounding check (should be 1.00)
                    .append(true).append(';')
                    .newLine();
        }

        String actual = baos.toString(StandardCharsets.ISO_8859_1);
        // Обратите внимание: newLine() зависит от ОС, в тесте заменим на \n для проверки
        if (System.lineSeparator().length() > 1) {
            actual = actual.replace(System.lineSeparator(), "\n");
        }

        String expected = "Round;100;1234567890123;-55;123.46;1.00;true;\n";

        if (!expected.equals(actual)) {
            throw new RuntimeException("Test Failed!\nExpected: " + expected + "\nActual:   " + actual);
        }
    }

    /**
     * Сценарий стандартной записи (как было у вас раньше).
     * Создает много мусора (String objects).
     */
    private static long runStandardScenario(OutputStream out, int iterations) throws IOException {
        // Эмулируем данные
        long round = 1000;
        int spot = 45123;
        double roi = 85.543;

        // Используем большой буфер, чтобы тестировать именно форматирование, а не I/O
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), 32768)) {
            for (int i = 0; i < iterations; i++) {
                writer.write(Long.toString(round + i));
                writer.write(';');
                writer.write(Integer.toString(spot));
                writer.write(';');
                writer.write(Double.toString(roi)); // Стандартный toString медленный
                writer.write(';');
                writer.write("LIVE");
                writer.newLine();
            }
        }
        return 0; // Для NullOutputStream размер не важен
    }

    /**
     * Сценарий с FastTextWriter.
     * Не создает объектов String во время цикла.
     */
    private static long runFastScenario(OutputStream out, int iterations) throws IOException {
        long round = 1000;
        int spot = 45123;
        double roi = 85.543;

        try (FastTextWriter writer = new FastTextWriter(out, 32768)) {
            for (int i = 0; i < iterations; i++) {
                writer.append(round + i)
                        .append(';')
                        .append(spot)
                        .append(';')
                        .append(roi, 2)
                        .append(';')
                        .append("LIVE")
                        .newLine();
            }
        }
        return 0;
    }

    private static void printResults(String name, long timeNs, long bytes) {
        double timeMs = timeNs / 1_000_000.0;
        double opsPerSec = (ITERATIONS / timeMs) * 1000;
        System.out.printf("%-35s: %8.2f ms | %,12.0f rows/sec%n", name, timeMs, opsPerSec);
    }

    /**
     * "Черная дыра" для данных. Нужна, чтобы измерить скорость формирования строк CPU,
     * а не скорость записи на жесткий диск (которая всегда медленная).
     */
    static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) {}
        @Override
        public void write(byte[] b) {}
        @Override
        public void write(byte[] b, int off, int len) {}
    }
}