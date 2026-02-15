package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.Predictor;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

public class EnsemblePredictorV3 implements Predictor {

    private final List<Predictor> predictors;

    public EnsemblePredictorV3(List<Predictor> predictors) {
        this.predictors = List.copyOf(predictors);
    }

    public static EnsemblePredictorV3 createDefaultEnsemble(int windowSize) {
        return new EnsemblePredictorV3(List.of(
//                new EfficientCountPredictor(windowSize),
//                new MultiOrderMarkovPredictor(windowSize),
                new PatternPredictor(windowSize),
                new SlidingWindowFreqPredictor(windowSize)
        ));
    }

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        EnumMap<Spot, Double> selectedSpotsMap = new EnumMap<>(Spot.class);
        int activePredictors = 0;
        for (Predictor predictor : predictors) {
            List<SpotPrediction> allPredictions = predictor.predictSpot(state, config);
            if (!allPredictions.isEmpty()) {
                activePredictors++;
                List<SpotPrediction> topK = allPredictions.stream()
                        .sorted(Comparator.comparingDouble(SpotPrediction::probability).reversed())
                        .limit(5)
                        .toList();
                for (SpotPrediction sp : topK) {
                    selectedSpotsMap.merge(sp.spot(), sp.probability(), Double::sum);
                }
            }
        }

        if (activePredictors == 0 || selectedSpotsMap.isEmpty()) {
            return List.of();
        }
        double totalSum = selectedSpotsMap.values()
                .stream()
                .mapToDouble(v -> v)
                .sum();
        return selectedSpotsMap.entrySet()
                .stream()
                .map(entry -> SpotPrediction.of(entry.getKey(), entry.getValue() / totalSum))
                .toList();
    }
}