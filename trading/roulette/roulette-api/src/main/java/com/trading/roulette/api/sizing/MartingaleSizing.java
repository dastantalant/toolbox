package com.trading.roulette.api.sizing;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.model.SpotPrediction;

public class MartingaleSizing implements Sizing {
    public static MartingaleSizing of() {
        return new MartingaleSizing();
    }

    @Override
    public long calculate(IStrategy strategy, SpotPrediction prediction, boolean isLastWin, long lastBet) {
        return isLastWin || lastBet == 0 ? strategy.getMinBet() : Math.min(lastBet * 2, strategy.getMaxBet());
    }
}