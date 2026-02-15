package com.trading.roulette.api.risk.limit;

import com.trading.roulette.api.IStrategy;

public interface BetLimitRule {
    default long calcMin(long baseMin, long tableMax, IStrategy strategy) {
        return baseMin;
    }

    default long calcMax(long actualMin, long baseMax, IStrategy strategy) {
        return baseMax;
    }
}