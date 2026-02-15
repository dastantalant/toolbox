package com.trading.roulette.api.stop;

import com.trading.roulette.api.IStrategy;

import java.util.Objects;

public class StreakStopCondition implements StopCondition {
    private final int maxLossStreak;
    private final int maxWinStreak;
    private int currentLossStreak = 0;
    private int currentWinStreak = 0;
    private long lastBalance;

    public StreakStopCondition(Integer maxLossStreak, Integer maxWinStreak, Long startBalance) {
        this.maxLossStreak = Objects.requireNonNullElse(maxLossStreak, 0);
        this.maxWinStreak = Objects.requireNonNullElse(maxWinStreak, 0);
        this.lastBalance = Objects.requireNonNull(startBalance);
    }

    @Override
    public boolean should(IStrategy strategy) {
        long curBalance = strategy.getVirtualBalance();
        if (curBalance == lastBalance) {
            return false;
        }
        if (curBalance < lastBalance) {
            currentLossStreak++;
            currentWinStreak = 0;
        } else {
            currentWinStreak++;
            currentLossStreak = 0;
        }
        lastBalance = curBalance;

        if (maxLossStreak > 0 && currentLossStreak >= maxLossStreak) return true;
        return maxWinStreak > 0 && currentWinStreak >= maxWinStreak;
    }
}