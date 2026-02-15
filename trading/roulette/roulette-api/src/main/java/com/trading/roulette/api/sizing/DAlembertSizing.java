package com.trading.roulette.api.sizing;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.model.SpotPrediction;

public class DAlembertSizing implements Sizing {

    public static DAlembertSizing of() {
        return new DAlembertSizing();
    }

    @Override
    public long calculate(IStrategy strategy, SpotPrediction prediction, boolean isLastWin, long lastBet) {
        long min = strategy.getMinBet();
        long max = strategy.getMaxBet();

        if (lastBet == 0) {
            return min;
        }

        long nextBet;

        if (isLastWin) {
            nextBet = lastBet - min;
            if (nextBet < min) {
                nextBet = min;
            }
        } else {
            nextBet = lastBet + min;
        }

        return Math.min(nextBet, max);
    }
}