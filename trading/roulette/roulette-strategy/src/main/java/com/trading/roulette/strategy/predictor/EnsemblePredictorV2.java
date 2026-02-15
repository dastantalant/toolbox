package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.BetTypePrediction;
import com.trading.roulette.api.model.ColorPrediction;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.Predictor;

import java.util.*;

public class EnsemblePredictorV2 implements Predictor {

    private final List<Predictor> predictors;

    public EnsemblePredictorV2(List<Predictor> predictors) {
        this.predictors = List.copyOf(predictors);
    }

    public static EnsemblePredictorV2 createDefaultEnsemble(int windowSize) {
        return new EnsemblePredictorV2(List.of(
                new EfficientCountPredictor(windowSize),
                new MultiOrderMarkovPredictor(windowSize),
                new PatternPredictor(windowSize),
                new SlidingWindowFreqPredictor(windowSize)
        ));
    }

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        // Map to hold the union of best spots.
        // We sum probabilities if multiple predictors pick the same spot.
        EnumMap<Spot, Double> selectedSpotsMap = new EnumMap<>(Spot.class);
        int activePredictors = 0;

        for (Predictor predictor : predictors) {
            List<SpotPrediction> allPredictions = predictor.predictSpot(state, config);

            if (!allPredictions.isEmpty()) {
                activePredictors++;

                // 1. Sort predictions for THIS predictor
                // 2. Take only the top K (spotsPerPredictor)
                List<SpotPrediction> topK = allPredictions.stream()
                        .sorted(Comparator.comparingDouble(SpotPrediction::probability).reversed())
                        .limit(2)
                        .toList();

                // 3. Add these specific spots to the aggregate map
                for (SpotPrediction sp : topK) {
                    selectedSpotsMap.merge(sp.spot(), sp.probability(), Double::sum);
                }
            }
        }

        if (activePredictors == 0 || selectedSpotsMap.isEmpty()) {
            return List.of();
        }

        final int count = activePredictors;
        double totalSum = selectedSpotsMap.values().stream().mapToDouble(v -> v).sum();

        // Normalize and return the selected set
        return selectedSpotsMap.entrySet().stream()
                .map(entry -> SpotPrediction.of(entry.getKey(), entry.getValue() / totalSum))
                // We do NOT filter by threshold here, we return all selected candidates
                .toList();
    }

    @Override
    public List<BetTypePrediction> predictType(State state, Config config) {
        // Logic adapted for BetTypes (Top K types from each predictor)
        EnumMap<BetType, Double> selectedTypesMap = new EnumMap<>(BetType.class);
        int active = 0;

        for (Predictor predictor : predictors) {
            List<BetTypePrediction> list = predictor.predictType(state, config);
            if (!list.isEmpty()) {
                active++;
                List<BetTypePrediction> topK = list.stream()
                        .sorted(Comparator.comparingDouble(BetTypePrediction::probability).reversed())
                        .limit(2)
                        .toList();

                topK.forEach(btp -> selectedTypesMap.merge(btp.type(), btp.probability(), Double::sum));
            }
        }

        if (active == 0 || selectedTypesMap.isEmpty()) return List.of();

        double totalSum = selectedTypesMap.values().stream().mapToDouble(v -> v).sum();

        return selectedTypesMap.entrySet().stream()
                .map(entry -> BetTypePrediction.of(entry.getKey(), entry.getValue() / totalSum))
                .toList();
    }

    @Override
    public ColorPrediction predictColor(State state, Config config) {
        // Color prediction usually returns just one object with 3 probs,
        // so "Top K" doesn't apply the same way. We average them.
        double red = 0;
        double black = 0;
        double green = 0;
        int active = 0;

        for (Predictor predictor : predictors) {
            ColorPrediction color = predictor.predictColor(state, config);
            if (color != null) {
                active++;
                red += color.red();
                black += color.black();
                green += color.green();
            }
        }

        if (active == 0) return ColorPrediction.of();
        return ColorPrediction.of(red / active, black / active, green / active);
    }
}