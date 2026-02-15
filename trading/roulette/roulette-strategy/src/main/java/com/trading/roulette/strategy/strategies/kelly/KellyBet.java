package com.trading.roulette.strategy.strategies.kelly;

import com.trading.roulette.api.enums.BetType;

public record KellyBet(BetType type, double fraction) {
    public static KellyBet of(BetType type, double fraction) {
        return new KellyBet(type, fraction);
    }
}
