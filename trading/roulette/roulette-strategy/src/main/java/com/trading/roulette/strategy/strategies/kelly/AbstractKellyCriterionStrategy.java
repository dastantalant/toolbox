package com.trading.roulette.strategy.strategies.kelly;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.utils.BetHelper;

import java.util.Optional;

@Strategy
public abstract class AbstractKellyCriterionStrategy extends AbstractStrategy {
    protected final double kelly;
    protected final double maxBetPercentageCap;
    protected final double minProbabilityThreshold;
    protected final Predictor predictor;

    protected AbstractKellyCriterionStrategy(Config config) {
        this(config, KellyContext.of());
    }

    protected AbstractKellyCriterionStrategy(Config config, KellyContext kContext) {
        super(config);
        kelly = kContext.getKelly();
        maxBetPercentageCap = kContext.getMaxBetPercentageCap();
        minProbabilityThreshold = kContext.getMinProbabilityThreshold();
        predictor = kContext.getPredictor();
    }

    protected Optional<KellyBet> calculateKellyFraction(SpotPrediction prediction, double kelly) {
        BetType type = BetHelper.straightUp(prediction.spot());
        double prob = prediction.probability();
        double payout = type.payout();
        if (payout > 0) {
            double kellyFraction = (payout * prob - (1 - prob)) / payout;
            if (kellyFraction > 0) {
                return Optional.of(KellyBet.of(type, kellyFraction * kelly));
            }
        }
        return Optional.empty();
    }
}
