package com.dastan.toolbox.excel.test;

import com.dastan.toolbox.excel.api.RowData;
import com.dastan.toolbox.excel.fast.FastExcelSource;
import com.dastan.toolbox.excel.poi.PoiXlsxSource;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XlsxSourceTest {

    @TempDir
    Path tempDir;

    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        testFile = tempDir.resolve("performance_test.xlsx").toFile();
        createReferenceExcelFile(testFile);
    }

    @Test
    @DisplayName("FastXlsxSource (Aalto/Zip) should read standard POI-generated file correctly")
    void testFastSourceReading() {
        try (FastExcelSource source = new FastExcelSource(testFile)) {
            List<RowData> rows = source.stream().toList();

            // 1. Проверка общего количества строк (Header + 3 data rows)
            assertThat(rows).hasSize(4);

            // 2. Проверка заголовков (Row 0)
            RowData header = rows.get(0);
            assertThat(header.get(0)).isEqualTo("ID");
            assertThat(header.get(1)).isEqualTo("Name");
            assertThat(header.hasColumn("IsActive")).isTrue();

            // 3. Проверка полной строки (Row 1: 101, Alice, true, 2025-01-01)
            RowData rowAlice = rows.get(1);

            // Числа в XML могут быть "101" или "101.0", проверяем гибко
            assertThat(rowAlice.get("ID")).startsWith("101");
            assertThat(rowAlice.get("Name")).isEqualTo("Alice");
            // Наш парсер маппит boolean "1" -> "true"
            assertThat(rowAlice.get("IsActive")).isEqualTo("true");
            // Наш парсер дат возвращает ISO-8601 LocalDateTime
            assertThat(rowAlice.get("Date")).startsWith("2025-01-01");

            // 4. Проверка пропусков (Row 2: Bob, без даты)
            RowData rowBob = rows.get(2);
            assertThat(rowBob.get("Name")).isEqualTo("Bob");
            assertThat(rowBob.get("Date")).isNull(); // Пустая ячейка должна быть null

            // 5. Проверка boolean false (Row 2)
            assertThat(rowBob.get("IsActive")).isEqualTo("false");
        }
    }

    @Test
    @DisplayName("PoiXlsxSource (Reference) should match FastXlsxSource logic")
    void testCompareImplementations() {
        List<RowData> fastRows;
        List<RowData> poiRows;

        try (var fast = new FastExcelSource(testFile)) {
            fastRows = fast.stream().toList();
        }
        try (var slow = new PoiXlsxSource(testFile)) {
            poiRows = slow.stream().toList();
        }

        // Структурная проверка
        assertThat(fastRows).hasSameSizeAs(poiRows);

        // Проверка контента (Name)
        assertThat(fastRows).zipSatisfy(poiRows, (fast, slow) -> {
            assertThat(fast.get("Name")).isEqualTo(slow.get("Name"));

            // Примечание: ID и Date могут отличаться форматом (строка vs double, формат даты),
            // поэтому сравниваем только стабильные типы данных (String)
        });
    }

    /**
     * Создает "эталонный" файл с помощью Apache POI.
     * Мы используем его, чтобы убедиться, что наш Fast Parser совместим со стандартным форматом.
     */
    private void createReferenceExcelFile(File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            // Стили для даты
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));

            // --- HEADER (Row 0) ---
            Row h = sheet.createRow(0);
            h.createCell(0).setCellValue("ID");
            h.createCell(1).setCellValue("Name");
            h.createCell(2).setCellValue("IsActive");
            h.createCell(3).setCellValue("Date");

            // --- DATA 1 (Row 1) ---
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(101); // Number
            r1.createCell(1).setCellValue("Alice"); // Shared String
            r1.createCell(2).setCellValue(true); // Boolean
            Cell d1 = r1.createCell(3);
            d1.setCellValue(LocalDate.of(2025, 1, 1)); // Date
            d1.setCellStyle(dateStyle);

            // --- DATA 2 (Row 2) ---
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue(102);
            r2.createCell(1).setCellValue("Bob");
            r2.createCell(2).setCellValue(false);
            // Date is missing (null)

            // --- DATA 3 (Row 3 - Inline String Test if possible, POI uses Shared by default) ---
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue(103);
            r3.createCell(1).setCellValue("Charlie");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }
}