package com.trading.roulette.strategy.strategies.hybrid;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.SpotPrediction;

import java.util.List;

@Strategy(tags = {Tag.HYBRID})
public class SelfTuningHybridStrategy extends AbstractHybridPredictorStrategy {

    public SelfTuningHybridStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        if (getState().getMarket().getCurrentLoopCount() > 0 && getState().getMarket().getCurrentLoopCount() % window == 0 && getBetCount() > 0) {
            adjustParameters();
        }

        List<SpotPrediction> top = getTop( markov, count, dynamicConfidence);
        if (top.isEmpty()) return List.of();
        long budget = (long) (getVirtualBalance() * dynamicFraction);
        return process( top, budget);
    }

    private void adjustParameters() {
        double average = getAverage();
        if (average < lowWinRate) {
            dynamicConfidence = Math.min(confidence * 1.25, 0.15);
            dynamicFraction = Math.max(fraction * 0.5, 0.01);
        } else if (average > highWinRate) {
            dynamicConfidence = Math.max(confidence * 0.9, 0.05);
            dynamicFraction = Math.min(fraction * 1.1, 0.075);
        } else {
            if (dynamicConfidence != confidence || dynamicFraction != fraction) {
                dynamicConfidence = confidence;
                dynamicFraction = fraction;
            }
        }
        resetCount();
    }
}