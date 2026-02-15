package com.trading.roulette.strategy.strategies;


import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.strategy.predictor.SlidingWindowFreqPredictor;

import java.util.Collections;
import java.util.List;

@Strategy(tags = {Tag.STATISTICAL, Tag.PROGRESSIVE})
public class ContinuousTrendStrategy extends AbstractStrategy {

    private final Predictor predictor;
    private final double betThreshold = 0.035;
    private final int spotsToBet = 3;

    public ContinuousTrendStrategy(Config config) {
        super(config);
        predictor = new SlidingWindowFreqPredictor(2048);
    }

    @Override
    public List<Bet> decide() {
        List<SpotPrediction> predictions = predictor.predictSpot(getState(), getConfig());

        if (predictions.isEmpty()) {
            return Collections.emptyList();
        }

        if (predictions.size() < spotsToBet) {
            return Collections.emptyList();
        }

        return predictions.stream()
                .limit(spotsToBet)
//                .filter(p -> p.probability() > betThreshold)
                .map(p -> Bet.of(BetType.straightUpBetFor(p.spot()), getMinBet()))
                .toList();
    }
}
