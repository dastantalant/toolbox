package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.Predictor;

import java.util.Deque;
import java.util.List;
import java.util.Objects;

@Deprecated
public class DifferencePredictor implements Predictor {

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        Deque<Spot> history = state.getMarket().getHistory();
        if (history.isEmpty()) {
            return List.of();
        }
        double average = history.stream()
                .mapToInt(Spot::number)
                .average()
                .orElse(0.0);
        int lastSpot = Objects.requireNonNull(state.getMarket().getLastSpot()).number();
        Spot spot1 = Spot.byNumber((int) (lastSpot + average));
        Spot spot2 = Spot.byNumber((int) (lastSpot - average));
        return SpotPrediction.of(spot1, 0.5, spot2, 0.5);
    }
}