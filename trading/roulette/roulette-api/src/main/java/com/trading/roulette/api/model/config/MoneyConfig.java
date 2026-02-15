package com.trading.roulette.api.model.config;

public record MoneyConfig(
        Type type,
        Double multiplier,
        Long step
) {
    public enum Type {
        FLAT,
        MARTINGALE,
        DALEMBERT,
        KELLY
    }

    public static MoneyConfig flat() {
        return new MoneyConfig(Type.FLAT, 0.0, 0L);
    }
}