package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.utils.BetHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Strategy(tags = {Tag.PREDICTIVE})
public class StraightUpStrategy extends AbstractStrategy {

    private final Predictor predictor;

    public StraightUpStrategy(Config config) {
        super(config);
        predictor = new MultiOrderMarkovPredictor();
    }

    @Override
    public List<Bet> decide() {
        Optional<SpotPrediction> optional = predictor.predictSpot(getState(), getConfig()).stream()
                .max(Comparator.comparingDouble(SpotPrediction::probability));
        if (optional.isEmpty()) {
            return List.of();
        }
        Spot spot = optional.get().spot();
        return Bet.list(BetHelper.straightUp(spot), getMinBet());
    }
}