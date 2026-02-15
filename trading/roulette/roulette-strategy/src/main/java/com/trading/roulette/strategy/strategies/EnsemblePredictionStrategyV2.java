package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.strategy.predictor.EnsemblePredictorV3;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.utils.BetHelper;

import java.util.ArrayList;
import java.util.List;

@Strategy(tags = {Tag.PREDICTIVE, Tag.HYBRID, Tag.AI})
public class EnsemblePredictionStrategyV2 extends AbstractStrategy {

    private final Predictor predictor;

    public EnsemblePredictionStrategyV2(Config config) {
        this(config, 300);
    }

    public EnsemblePredictionStrategyV2(Config config, int windowSize) {
        super(config);
        predictor = EnsemblePredictorV3.createDefaultEnsemble(windowSize);
    }

    @Override
    public List<Bet> decide() {
        List<SpotPrediction> predictions = predictor.predictSpot(getState(), getConfig());
        if (predictions.isEmpty()) {
            return List.of();
        }
        List<Bet> bets = new ArrayList<>();
        long fixedBetAmount = getMinBet();
        if (fixedBetAmount <= 0 || fixedBetAmount > getVirtualBalance()) {
            return List.of();
        }
        for (SpotPrediction prediction : predictions) {
            bets.add(BetHelper.betting(prediction, fixedBetAmount));
        }
        return bets;
    }
}