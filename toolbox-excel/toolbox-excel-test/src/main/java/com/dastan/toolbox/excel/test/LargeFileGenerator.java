package com.dastan.toolbox.excel.test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

public class LargeFileGenerator {

    public static void main(String[] args) throws IOException {
        String filename = "million_rows_test.xlsx";
        System.out.println("Generating " + filename + "...");
        long start = System.currentTimeMillis();

        generate(filename, 1_000_000);

        long end = System.currentTimeMillis();
        System.out.printf("Done! Time: %.2f seconds.%n", (end - start) / 1000.0);
    }

    public static void generate(String filePath, int rowCount) throws IOException {
        // keep 100 rows in memory, exceeding rows will be flushed to disk
        try (SXSSFWorkbook wb = new SXSSFWorkbook(100)) {
            // Опционально: сжимать временные файлы (экономит место на диске, но чуть медленнее)
            wb.setCompressTempFiles(true);

            Sheet sheet = wb.createSheet("BigData");

            // --- Настройка стилей ---
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd"));

            CellStyle dateTimeStyle = wb.createCellStyle();
            dateTimeStyle.setDataFormat(wb.createDataFormat().getFormat("yyyy-mm-dd h:mm:ss"));

            CellStyle currencyStyle = wb.createCellStyle();
            currencyStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0.00 \"$\""));

            // --- Заголовки ---
            Row header = sheet.createRow(0);
            String[] headers = {
                    "ID (Int)", "Name (String)", "Active (Bool)", "Date (LocalDate)",
                    "DateTime (LocalDateTime)", "Salary (Double)", "Formula", "Empty", "Large Text"
            };

            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            // --- Генерация данных ---
            Random random = new Random();
            LocalDate baseDate = LocalDate.of(2020, 1, 1);

            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.createRow(i);

                // 1. Integer
                row.createCell(0).setCellValue(i);

                // 2. String (Mix of repeating and unique to test String Pool)
                String name = (i % 10 == 0) ? "Manager" : "User_" + i;
                row.createCell(1).setCellValue(name);

                // 3. Boolean
                row.createCell(2).setCellValue(i % 2 == 0);

                // 4. Date
                Cell dateCell = row.createCell(3);
                dateCell.setCellValue(baseDate.plusDays(i % 3650));
                dateCell.setCellStyle(dateStyle);

                // 5. DateTime
                Cell dateTimeCell = row.createCell(4);
                dateTimeCell.setCellValue(LocalDateTime.now().minusMinutes(i));
                dateTimeCell.setCellStyle(dateTimeStyle);

                // 6. Double / Currency
                Cell sumCell = row.createCell(5);
                sumCell.setCellValue(random.nextDouble() * 10000);
                sumCell.setCellStyle(currencyStyle);

                // 7. Formula (Just a string in streaming API, usually not evaluated)
                // Пример: A[i] * 2
                Cell formulaCell = row.createCell(6);
                formulaCell.setCellFormula("A" + (i + 1) + "*2");

                // 8. Empty cell (skip creation or create blank)
                if (i % 100 != 0) { // Каждая 100-я будет пустой
                    row.createCell(7).setCellValue("Not Empty");
                }

                // 9. Large Text (для теста буфера чтения)
                if (i % 1000 == 0) {
                    row.createCell(8).setCellValue("Lorem ipsum dolor sit amet ".repeat(10));
                }

                // Логирование прогресса
                if (i % 100_000 == 0) {
                    System.out.println("Generated " + i + " rows...");
                }
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }

            // Удаляем временные файлы с диска
            wb.dispose();
        }
    }
}