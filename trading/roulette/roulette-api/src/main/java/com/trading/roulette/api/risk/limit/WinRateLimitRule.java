package com.trading.roulette.api.risk.limit;

import com.trading.roulette.api.IStrategy;

public class WinRateLimitRule implements BetLimitRule {
    private final double minWinRate;
    private final long minRounds;

    /**
     * @param minWinRate Integer 0-100 (e.g. 45 = 45% win rate). Clamped to 100.
     * @param minRounds rounds count
     */
    public WinRateLimitRule(int minWinRate, long minRounds) {
        this.minWinRate = Math.min(minWinRate, 100) / 100.0;
        this.minRounds = minRounds;
    }

    @Override
    public long calcMax(long currentMin, long currentMax, IStrategy strategy) {
        if (strategy.getBetCount() < minRounds) {
            return currentMax;
        }
        double currentRate = (double) strategy.getWonCount() / strategy.getBetCount();
        if (currentRate < minWinRate) {
            return currentMin;
        }
        return currentMax;
    }
}