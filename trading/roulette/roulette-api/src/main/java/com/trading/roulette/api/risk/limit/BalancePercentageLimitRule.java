package com.trading.roulette.api.risk.limit;

import com.trading.roulette.api.IStrategy;

public class BalancePercentageLimitRule implements BetLimitRule {
    private final double percentage;

    /**
     * @param percentage Integer value 0-100 (e.g., 5 = 5%). Clamped to 100.
     */
    public BalancePercentageLimitRule(int percentage) {
        this.percentage = Math.min(percentage, 100) / 100.0;
    }

    @Override
    public long calcMax(long currentMin, long currentMax, IStrategy strategy) {
        long balance = strategy.getVirtualBalance();
        if (balance <= 0) return 0;
        long calculatedMax = (long) (balance * percentage);
        long newMax = Math.max(calculatedMax, currentMin);
        newMax = Math.min(newMax, currentMax);
        return newMax;
    }
}