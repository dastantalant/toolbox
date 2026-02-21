package com.toolbox.csv;

import com.toolbox.core.logging.FastLogger;
import com.toolbox.csv.annotation.CsvColumn;
import com.toolbox.csv.annotation.CsvFile;
import com.toolbox.csv.annotation.CsvIgnore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Deprecated
public class CsvDemo {

    public static void main(String[] args) throws Throwable {
        FastLogger.info("=== TEST 1: Annotated Class (Custom Separator & UTF-8) ===");
        testAnnotatedUser();
        FastLogger.info("\n=== TEST 2: Record without Annotations (Defaults) ===");
        testSimpleRecord();
    }

    private static void testAnnotatedUser() throws Throwable {
        List<UserDto> users = Arrays.asList(new UserDto(100, "Иван Иванов", 500.50, true),
                new UserDto(101, "John \"The Rock\" Doe", 1000.00, false),
                new UserDto(102, "Алина", 0.0, true));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CsvWriter<UserDto> writer = new CsvWriter<>(UserDto.class);
        writer.write(users, out);
        String csvOutput = out.toString(StandardCharsets.UTF_8);
        FastLogger.info("--- Generated CSV Content ---");
        FastLogger.info(csvOutput);
        FastLogger.info("-----------------------------");
        ByteArrayInputStream in = new ByteArrayInputStream(csvOutput.getBytes(StandardCharsets.UTF_8));
        CsvReader<UserDto> reader = new CsvReader<>(UserDto.class);
        List<UserDto> readBack = reader.read(in);
        FastLogger.info("Read " + readBack.size() + " items.");
        readBack.forEach(item -> FastLogger.info(item.toString()));
    }

    private static void testSimpleRecord() throws Throwable {
        List<SimpleItem> items = Arrays.asList(new SimpleItem("Laptop", 1500),
                new SimpleItem("Mouse", 25));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new CsvWriter<>(SimpleItem.class).write(items, out);
        String csvOutput = out.toString(StandardCharsets.UTF_8);
        FastLogger.info("--- Generated CSV Content ---");
        FastLogger.info(csvOutput);
        FastLogger.info("-----------------------------");
        ByteArrayInputStream in = new ByteArrayInputStream(csvOutput.getBytes(StandardCharsets.UTF_8));
        List<SimpleItem> readBack = new CsvReader<>(SimpleItem.class).read(in);
        readBack.forEach(item -> FastLogger.info(item.toString()));
    }

    @CsvFile(separator = Separator.SEMICOLON)
    public static class UserDto {
        @CsvColumn(order = 1, value = "ID")
        private int id;
        @CsvColumn(order = 2, value = "Full Name")
        private String name;
        private double balance;
        @CsvIgnore
        private boolean active;

        public UserDto() {
        }

        public UserDto(int id, String name, double balance, boolean active) {
            this.id = id;
            this.name = name;
            this.balance = balance;
            this.active = active;
        }

        public String toString() {
            return "UserDto{id=" + id + ", name='" + name + "', balance=" + balance + "}";
        }

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

    public record SimpleItem(String title, int price) {
    }
//    public static class SimpleItem {
//        private String title;
//        private int price;
//        public SimpleItem() {}
//        public SimpleItem(String title, int price) {this.title = title;this.price = price;}
//        public String title() {return title;}
//        public String getTitle() {return title;}
//        public void title(String title) {this.title = title;}
//        public void setTitle(String title) {this.title = title;}
//        public int price() {return price;}
//        public int getPrice() {return price;}
//        public void price(int price) {this.price = price;}
//        public void setPrice(int price) {this.price = price;}
//    }
}