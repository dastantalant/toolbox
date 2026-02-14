package com.dastan.toolbox.excel.test;

import com.dastan.toolbox.excel.api.annotation.ExcelColumn;
import com.dastan.toolbox.excel.api.annotation.ExcelSheet;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ExcelSheet
public class TestData {

    @ExcelColumn("ID (Int)")
    private int id;

    @ExcelColumn("Name (String)")
    private String name;

    @ExcelColumn("Active (Bool)")
    private boolean active;

    @ExcelColumn("Date (LocalDate)")
    private LocalDate localDate;

    @ExcelColumn("DateTime (LocalDateTime)")
    private LocalDateTime localDateTime;

    @ExcelColumn("Salary (Double)")
    private double salary;

    @ExcelColumn("Formula")
    private String formula;

    @ExcelColumn("Large Text")
    private String largeText;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getLargeText() {
        return largeText;
    }

    public void setLargeText(String largeText) {
        this.largeText = largeText;
    }
}