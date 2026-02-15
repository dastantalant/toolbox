package com.trading.roulette.api.model;

import com.trading.roulette.api.enums.BetType;

public record BetTypePrediction(BetType type, double probability) {
    public static BetTypePrediction of(BetType type, double probability) {
        return new BetTypePrediction(type, probability);
    }
}