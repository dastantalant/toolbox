package com.trading.roulette.strategy;

import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.utils.BetHelper;
import com.trading.roulette.strategy.predictor.PatternPredictor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BettingHelper {
    private BettingHelper() {
    }

    private static final Predictor ADVISOR_PREDICTOR = new PatternPredictor();
    private static final int NUMBER_OF_BETS_TO_PLACE = 5;

    public static List<Bet> createDiversifiedBets(long total, State state, Config config) {
        long minBet = config.minBet();

        if (total < minBet) {
            return List.of();
        }

        List<SpotPrediction> predictions = ADVISOR_PREDICTOR.predictSpot(state, config);

        if (predictions.isEmpty()) {
            return List.of();
        }

        List<SpotPrediction> top = predictions.stream()
                .sorted(Comparator.comparingDouble(SpotPrediction::probability).reversed())
                .limit(NUMBER_OF_BETS_TO_PLACE)
                .toList();

        if (top.isEmpty()) {
            return List.of();
        }

        double sum = top.stream()
                .mapToDouble(SpotPrediction::probability)
                .sum();

        if (sum == 0) {
            return List.of();
        }

        List<Bet> bets = new ArrayList<>(NUMBER_OF_BETS_TO_PLACE);
        for (SpotPrediction item : top) {
            long betValue = (long) (total * (item.probability() / sum));

            if (betValue >= 1L) {
                bets.add(Bet.of(BetHelper.straightUp(item.spot()), betValue));
            }
        }
        return bets;
    }
}