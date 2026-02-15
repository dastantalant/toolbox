package com.trading.roulette.api.model;

public record ColorPrediction(double red, double black, double green) {

    public static ColorPrediction of() {
        return new ColorPrediction(0.0, 0.0, 0.0);
    }

    public static ColorPrediction of(double probability) {
        return new ColorPrediction(probability, probability, probability);
    }

    public static ColorPrediction of(double red, double black, double green) {
        return new ColorPrediction(red, black, green);
    }
}