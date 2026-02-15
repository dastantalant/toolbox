package com.trading.roulette.api.model;

import com.trading.roulette.api.enums.Spot;

import java.util.List;

public record SpotPrediction(Spot spot, double probability) {
    public static SpotPrediction of(Spot spot, double probability) {
        return new SpotPrediction(spot, probability);
    }

    public static List<SpotPrediction> of(Spot spot1, double probability1, Spot spot2, double probability2) {
        return List.of(new SpotPrediction(spot1, probability1), new SpotPrediction(spot2, probability2));
    }

    public String show() {
        return String.format("%2s=%.2f", spot.number(), probability);
    }

    public String toString() {
        return String.format("%7s=%.3f", spot, probability);
    }
}