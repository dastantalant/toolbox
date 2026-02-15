package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.utils.HistoryUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BayesianPredictor implements Predictor {
    private static final double PRIOR_WEIGHT = 1.0;
    private final int windowSize;

    public BayesianPredictor() {
        this(500);
    }

    public BayesianPredictor(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        List<Spot> window = HistoryUtil.window(state, windowSize);
        long totalSpins = window.size();
        Map<Spot, Long> frequencies = window.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<Spot> availableSpots = Spot.availableList(config.rouletteType());
        double denominator = totalSpins + (availableSpots.size() * PRIOR_WEIGHT);

        return availableSpots.stream()
                .map(spot -> {
                    long count = frequencies.getOrDefault(spot, 0L);
                    double probability = (count + PRIOR_WEIGHT) / denominator;
                    return SpotPrediction.of(spot, probability);
                })
                .sorted(Comparator.comparingDouble(SpotPrediction::probability).reversed())
                .toList();
    }
}