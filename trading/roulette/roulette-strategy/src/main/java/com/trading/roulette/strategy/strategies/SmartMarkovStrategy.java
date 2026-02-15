package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.utils.BetHelper;

import java.util.List;

@Strategy(tags = {Tag.PREDICTIVE})
public class SmartMarkovStrategy extends AbstractStrategy {
    private final Predictor predictor;
    private final double min;

    public SmartMarkovStrategy(Config config) {
        super(config);
        predictor = new MultiOrderMarkovPredictor();
        min = 0.05;
    }

    @Override
    public List<Bet> decide() {
        return predictor.predictSpot(getState(), getConfig()).stream()
                .filter(prediction -> prediction.probability() > min)
                .limit(3)
                .map(prediction -> Bet.of(BetHelper.straightUp(prediction.spot()), getMinBet()))
                .toList();
    }
}