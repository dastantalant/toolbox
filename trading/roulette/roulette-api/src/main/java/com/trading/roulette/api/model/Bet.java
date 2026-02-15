package com.trading.roulette.api.model;

import com.trading.roulette.api.enums.BetType;

import java.util.List;

public record Bet(BetType type, long value) {

    public static List<Bet> list(BetType type, long value) {
        return List.of(new Bet(type, value));
    }

    public static Bet of(BetType type, long value) {
        return new Bet(type, value);
    }

    public static List<Bet> of(BetType type1, long value1, BetType type2, long value2) {
        return List.of(of(type1, value1), of(type2, value2));
    }
}