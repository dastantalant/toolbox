package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.strategy.predictor.PatternPredictor;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.utils.BetHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Strategy(tags = {Tag.PATTERN})
public class PatternStrategy extends AbstractStrategy {

    private final double probability;
    private final int max;
    private final double fraction;
    private final double multiplier;

    private final Predictor predictor;

    public PatternStrategy(Config config) {
        super(config);
        predictor = new PatternPredictor();
        probability = 0.02;
        max = 5;
        fraction = 0.1;
        multiplier = 2.0;
    }

    @Override
    public List<Bet> decide() {
        long total = (long) Math.min(Math.max(getVirtualBalance(), getConfig().startBalance()) * fraction, getMaxBet());
        if (total < getMinBet()) {
            return List.of();
        }
        List<SpotPrediction> spot = predictor.predictSpot(getState(), getConfig());
        if (spot.isEmpty()) {
            return List.of();
        }

        List<Bet> bets = extract( spot, total);
        return (bets.isEmpty()) ? Bet.list(BetHelper.straightUp(spot.getFirst().spot()), getMinBet()) : bets;
    }

    private List<Bet> extract(List<SpotPrediction> predictions, long total) {
        List<SpotPrediction> sortedPredictions = new ArrayList<>(predictions);
        sortedPredictions.sort(Comparator.comparingDouble(SpotPrediction::probability).reversed());

        List<Bet> bets = new ArrayList<>(max);
        long budget = 0;
        long minBet = getMinBet();

        for (SpotPrediction item : sortedPredictions) {
            if (budget >= total || bets.size() >= max) {
                break;
            }
            long remaining = total - budget;
            if (remaining < minBet) {
                break;
            }
            if (item.probability() >= probability) {
                long calculatedValue = (long) (total * item.probability() * multiplier);
                long betValue = Math.clamp(calculatedValue, minBet, remaining);
                budget += betValue;
                bets.add(Bet.of(BetHelper.straightUp(item.spot()), betValue));
            }
        }
        return bets;
    }
}