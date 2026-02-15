package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;
import com.trading.roulette.api.AbstractStrategy;

import java.util.List;

@Strategy(tags = {Tag.PREDICTIVE})
public class MultiBetStrategy extends AbstractStrategy {

    private final Predictor predictor;

    public MultiBetStrategy(Config config) {
        super(config);
        predictor = new MultiOrderMarkovPredictor();
    }

    @Override
    public List<Bet> decide() {
        final long limit = Math.min(Math.max(getVirtualBalance(), getConfig().startBalance()) / 10, getMaxBet());
        return predictor.predictType(getState(), getConfig()).stream()
                .filter(pred -> pred.probability() > 0.3)
                .map(pred -> Bet.of(pred.type(), Math.max(getMinBet(), (long) (limit * pred.probability()))))
                .toList();
    }
}