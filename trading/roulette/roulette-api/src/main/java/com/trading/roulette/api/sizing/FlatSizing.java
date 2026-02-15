package com.trading.roulette.api.sizing;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.model.SpotPrediction;

public class FlatSizing implements Sizing {
    @Override
    public long calculate(IStrategy strategy, SpotPrediction prediction, boolean isLastWin, long lastBet) {
        return strategy.getMinBet();
    }
}