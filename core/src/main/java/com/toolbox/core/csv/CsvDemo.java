package com.toolbox.core.csv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Deprecated
public class CsvDemo {

    public static void main(String[] args) throws Throwable {
        System.out.println("=== TEST 1: Annotated Class (Custom Separator & UTF-8) ===");
        testAnnotatedUser();

        System.out.println("\n=== TEST 2: Record without Annotations (Defaults) ===");
        testSimpleRecord();
    }

    // --- СЦЕНАРИЙ 1: Класс с настройками ---
    private static void testAnnotatedUser() throws Throwable {
        List<UserDto> users = Arrays.asList(
                new UserDto(100, "Иван Иванов", 500.50, true),
                new UserDto(101, "John \"The Rock\" Doe", 1000.00, false), // Кавычки внутри
                new UserDto(102, "Алина", 0.0, true)
        );

        // 1. Запись
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvWriter<UserDto> writer = new CsvWriter<>(UserDto.class);
        writer.write(users, out);

        String csvOutput = out.toString(StandardCharsets.UTF_8);
        System.out.println("--- Generated CSV Content ---");
        System.out.print(csvOutput);
        System.out.println("-----------------------------");

        // 2. Чтение
        ByteArrayInputStream in = new ByteArrayInputStream(csvOutput.getBytes(StandardCharsets.UTF_8));
        CsvReader<UserDto> reader = new CsvReader<>(UserDto.class);
        List<UserDto> readBack = reader.read(in);

        // 3. Сверка
        System.out.println("Read " + readBack.size() + " items.");
        readBack.forEach(System.out::println);
    }

    // --- СЦЕНАРИЙ 2: Рекорд без аннотаций ---
    private static void testSimpleRecord() throws Throwable {
        List<SimpleItem> items = Arrays.asList(
                new SimpleItem("Laptop", 1500),
                new SimpleItem("Mouse", 25)
        );

        // 1. Запись
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CsvWriter<>(SimpleItem.class).write(items, out); // Работает без @CsvFile

        String csvOutput = out.toString(StandardCharsets.UTF_8);
        System.out.println("--- Generated CSV Content ---");
        System.out.print(csvOutput);
        System.out.println("-----------------------------");

        // 2. Чтение
        ByteArrayInputStream in = new ByteArrayInputStream(csvOutput.getBytes(StandardCharsets.UTF_8));
        List<SimpleItem> readBack = new CsvReader<>(SimpleItem.class).read(in);

        readBack.forEach(System.out::println);
    }

    // ================= DTO CLASSES =================

    // DTO с явными настройками: точка с запятой, порядок полей
    @CsvFile(separator = Separator.SEMICOLON)
    public static class UserDto {
        @CsvColumn(order = 1, value = "ID")
        private int id;

        @CsvColumn(order = 2, value = "Full Name")
        private String name;

        // Нет аннотации -> берется имя поля "balance", порядок случайный (но после order=1,2)
        private double balance;

        @CsvIgnore // Это поле не попадет в CSV
        private boolean active;

        // Обязателен пустой конструктор для обычных классов
        public UserDto() {
        }

        public UserDto(int id, String name, double balance, boolean active) {
            this.id = id;
            this.name = name;
            this.balance = balance;
            this.active = active;
        }

        @Override
        public String toString() {
            return "UserDto{id=" + id + ", name='" + name + "', balance=" + balance + "}";
        }

        // Геттеры и сеттеры нужны для Reflection (MethodHandle)
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }
    }

    // Java Record - работает "из коробки" без сеттеров и пустого конструктора
    public record SimpleItem(String title, int price) {
    }
}