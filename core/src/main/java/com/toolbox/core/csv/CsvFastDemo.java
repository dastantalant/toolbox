//package com.dastan.core.csv;
//
//
//import com.dastan.core.io.*;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
//import java.util.List;
//@Deprecated
//public class CsvFastDemo {
//
//    public static void main(String[] args) throws Throwable {
//        // Данные
//        List<Product> products = Arrays.asList(
//                new Product(1, "Super Gadget", 999.99),
//                new Product(2, "Wire \"Standard\"", 10.50), // Проверка экранирования кавычек
//                new Product(3, "Русский Текст", 0.0)        // Проверка UTF-8
//        );
//
//        System.out.println("=== 1. Writing to BufferedOutput ===");
//
//        // 1. Создаем ваш BufferedOutput (Growing Array)
//        ArrayBufferedOutput out = BufferedOutput.growing();
//
//        // 2. Пишем CSV
//        FastCsvWriter<Product> writer = new FastCsvWriter<>(Product.class);
//        writer.write(products, out);
//        out.close(); // Важно закрыть, чтобы данные стали доступны для чтения
//
//        // 3. Выводим результат записи (байты -> строка)
//        // Для ArrayBufferedOutput нужно использовать специфичный метод получения данных,
//        // но так как API частично скрыто, используем доступ через TextOutput или копирование
//
//        // Вариант А: Чтение через BufferedInput (которого нет в дампе, эмулируем)
//        // Вариант Б: Достаем байты из out (если есть метод, например toByteArray, но в интерфейсе Output его нет)
//        // Предположим, мы можем превратить это в ByteArray для теста:
//
//        // Хак для теста: достаем внутренний буфер (если бы был доступ) или используем буфер обмена.
//        // Так как BufferedOutput закрыт, мы не можем писать. Но ArrayBufferedOutput обычно имеет метод получения массива.
//        // В вашем коде ArrayBufferedOutput наследует BufferedOutput, но метод получения массива не показан в интерфейсе Output.
//        // Однако в методе growing() возвращается ArrayBufferedOutput, у которого (предположительно) есть метод получения данных.
//
//        // Для демонстрации я использую ByteArrayOutputStream и оберну его в BufferedOutput.of(out)
//        java.io.ByteArrayOutputStream javaBaos = new java.io.ByteArrayOutputStream();
//        Output fastOut = IOFactory.writerOf(javaBaos);
//
//        writer.write(products, fastOut);
//        fastOut.flush();
//        fastOut.close();
//
//        byte[] csvBytes = javaBaos.toByteArray();
//        String csvString = new String(csvBytes, StandardCharsets.UTF_8);
//        System.out.println("Result CSV:\n" + csvString);
//
//        System.out.println("=== 2. Reading from Input ===");
//
//        // Создаем Input из байтов (Эмуляция, так как нет реализации BufferedInput в дампе)
//        Input input = IOFactory.readerOf(csvBytes);
//        FastCsvReader<Product> reader = new FastCsvReader<>(Product.class);
//        List<Product> readBack = reader.read(input);
//
//        readBack.forEach(System.out::println);
//    }
//
//    public record Product(int id, String name, double price) {}
//}