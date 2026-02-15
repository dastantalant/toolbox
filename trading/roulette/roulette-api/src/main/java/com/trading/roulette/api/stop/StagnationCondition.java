package com.trading.roulette.api.stop;

import com.trading.roulette.api.IStrategy;

public class StagnationCondition implements StopCondition {
    private final int maxStagnationRounds;
    private int roundsWithoutChange;
    private long lastBalance;
    private boolean isInitialized;

    public StagnationCondition(int maxStagnationRounds) {
        this.maxStagnationRounds = maxStagnationRounds;
        this.roundsWithoutChange = 0;
        this.isInitialized = false;
    }

    @Override
    public boolean should(IStrategy strategy) {
        long currentBalance = strategy.getVirtualBalance();
        if (!isInitialized) {
            lastBalance = currentBalance;
            isInitialized = true;
            return false;
        }

        if (currentBalance == lastBalance) {
            roundsWithoutChange++;
        } else {
            roundsWithoutChange = 0;
            lastBalance = currentBalance;
        }
        return roundsWithoutChange >= maxStagnationRounds;
    }
}