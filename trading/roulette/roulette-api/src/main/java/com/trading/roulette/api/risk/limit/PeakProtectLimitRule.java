package com.trading.roulette.api.risk.limit;

import com.trading.roulette.api.IStrategy;

public class PeakProtectLimitRule implements BetLimitRule {
    private final double drawdownStep;
    private final double reductionFactor;

    /**
     * @param drawdownStep Integer 0-100 (e.g. 10 = 10% drawdown)
     * @param reductionFactor Integer 0-100 (e.g. 50 = reduce to 50%)
     */
    public PeakProtectLimitRule(int drawdownStep, int reductionFactor) {
        this.drawdownStep = Math.min(drawdownStep, 100) / 100.0;
        this.reductionFactor = Math.min(reductionFactor, 100) / 100.0;
    }

    @Override
    public long calcMax(long currentMin, long currentMax, IStrategy strategy) {
        long peak = strategy.getPeakBalance();
        long current = strategy.getVirtualBalance();
        if (peak <= 0 || current >= peak) return currentMax;
        double drawdown = (double) (peak - current) / peak;
        int steps = (int) (drawdown / drawdownStep);
        if (steps > 0) {
            long newMax = currentMax;
            for (int i = 0; i < steps; i++) {
                newMax = (long) (newMax * reductionFactor);
            }
            newMax = Math.max(newMax, currentMin);
            return newMax;
        }
        return currentMax;
    }
}