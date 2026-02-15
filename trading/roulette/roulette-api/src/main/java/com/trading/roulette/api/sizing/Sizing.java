package com.trading.roulette.api.sizing;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.model.SpotPrediction;

public interface Sizing {

    long calculate(IStrategy strategy, SpotPrediction prediction, boolean isLastWin, long lastBet);

}