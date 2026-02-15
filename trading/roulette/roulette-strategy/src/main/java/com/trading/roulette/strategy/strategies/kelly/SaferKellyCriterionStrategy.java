package com.trading.roulette.strategy.strategies.kelly;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.SpotPrediction;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Strategy(tags = {Tag.HYBRID, Tag.STATISTICAL})
public class SaferKellyCriterionStrategy extends AbstractKellyCriterionStrategy {

    public SaferKellyCriterionStrategy(Config config) {
        KellyContext build = KellyContext.builder()
                .kelly(0.1)
                .maxBetPercentageCap(0.05)
                .minProbabilityThreshold(0.028)
                .build();
        super(config, build);
    }

    @Override
    public List<Bet> decide() {
        List<SpotPrediction> predictions = predictor.predictSpot(getState(), getConfig());
        Optional<KellyBet> optional = predictions.stream()
                .filter(item -> item.probability() > minProbabilityThreshold)
                .map(prediction -> calculateKellyFraction(prediction, kelly))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparingDouble(KellyBet::fraction));

        if (optional.isPresent()) {
            KellyBet kellyBet = optional.get();
            long clamp = clamp(kellyBet.fraction());
            if (clamp >= getMinBet()) {
                return Bet.list(kellyBet.type(), clamp);
            }
        }
        return List.of();
    }

    private long clamp(double fraction) {
        long balance = getVirtualBalance();
        if (balance <= 0) {
            return 0;
        }
        double safeFraction = Math.min(fraction, maxBetPercentageCap);
        long proposed = (long) (balance * safeFraction);
        long clamped = Math.clamp(proposed, getMinBet(), getMaxBet());
        return Math.clamp(clamped, 0L, balance);
    }
}