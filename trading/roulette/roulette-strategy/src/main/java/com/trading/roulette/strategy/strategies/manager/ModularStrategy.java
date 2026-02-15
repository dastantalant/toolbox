package com.trading.roulette.strategy.strategies.manager;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.sizing.Sizing;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.utils.BetHelper;

import java.util.Comparator;
import java.util.List;

public abstract class ModularStrategy extends AbstractStrategy {

    private final Predictor predictor;
    private final Sizing sizing;

    protected ModularStrategy(Config config, Predictor predictor, Sizing sizing) {
        super(config, Manager.formatName(predictor, sizing));
        this.predictor = predictor;
        this.sizing = sizing;
    }

    public static ModularStrategy of(Config config, Predictor predictor, Sizing sizing) {
        return new ModularStrategy(config, predictor, sizing){};
    }

    @Override
    public List<Bet> decide() {
        List<SpotPrediction> predictions = predictor.predictSpot(getState(), getConfig());
        if (predictions.isEmpty()) return List.of();

        SpotPrediction bestPrediction = predictions.stream()
                .max(Comparator.comparingDouble(SpotPrediction::probability))
                .orElse(null);

        long amount = sizing.calculate(this, bestPrediction, isLastBetWon(), isLastBet() ? getLastBet() : 0);
        long balance = getVirtualBalance();
        if (amount <= 0 || amount > balance) {
            amount = Math.min(amount, balance);
            if (amount < getMinBet()) return List.of();
        }
        return Bet.list(BetHelper.straightUp(bestPrediction.spot()), amount);
    }
}
