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
public class KellyCriterionStrategy extends AbstractKellyCriterionStrategy {

    public KellyCriterionStrategy(Config config) {
        KellyContext build = KellyContext.builder()
                .kelly(0.3)
                .build();
        super(config, build);
    }

    @Override
    public List<Bet> decide() {
        List<SpotPrediction> predictions = predictor.predictSpot(getState(), getConfig());
        Optional<KellyBet> optional = predictions.stream()
                .map(prediction -> calculateKellyFraction(prediction, kelly))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparingDouble(KellyBet::fraction));

        if (optional.isPresent()) {
            KellyBet kellyBet = optional.get();
            long balance = getVirtualBalance();
            long clamp = Math.clamp((long) (balance * kellyBet.fraction()), getMinBet(), getMaxBet());
            if (clamp <= balance || clamp >= getMinBet()) {
                return Bet.list(kellyBet.type(), clamp);
            }
        }
        return List.of();

    }
}