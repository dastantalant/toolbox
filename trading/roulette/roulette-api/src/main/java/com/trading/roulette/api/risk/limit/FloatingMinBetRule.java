package com.trading.roulette.api.risk.limit;

import com.trading.roulette.api.IStrategy;

public class FloatingMinBetRule implements BetLimitRule {
    private final double balancePercent;

    /**
     * @param balancePercent Integer value 0-1000 (e.g., 1 = 1%). Clamped to 1000.
     */
    public FloatingMinBetRule(int balancePercent) {
        this.balancePercent = Math.min(balancePercent, 1000) / 1000.0;
    }

    @Override
    public long calcMin(long currentMin, long currentMax, IStrategy strategy) {
        long balance = strategy.getVirtualBalance();
        long calcMin = (long) (balance * balancePercent);
        long newMin = Math.max(currentMin, calcMin);
        if (newMin > currentMax) {
            newMin = currentMax;
        }
        return newMin;
    }
}