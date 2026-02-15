package com.trading.roulette.strategy.strategies.regime;

public class DoubleTriple {
    private final double first;
    private final double second;
    private final double third;

    public DoubleTriple(double first, double second, double third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public static DoubleTriple of(double first, double second, double third) {
        return new DoubleTriple(first, second, third);
    }

    public static DoubleTriple of() {
        return new DoubleTriple(0.0, 0.0, 0.0);
    }

    public double getFirst() {
        return first;
    }

    public double getSecond() {
        return second;
    }

    public double getThird() {
        return third;
    }
}
