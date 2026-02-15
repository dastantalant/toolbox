package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.BetTypePrediction;
import com.trading.roulette.api.model.ColorPrediction;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.Predictor;

import java.util.EnumMap;
import java.util.List;

public class EnsemblePredictor implements Predictor {

    private final List<Predictor> predictors;

    public EnsemblePredictor(List<Predictor> predictors) {
        this.predictors = List.copyOf(predictors);
    }

    /**
     * Создает ансамбль, где все предикторы настроены на одинаковое скользящее окно.
     * @param windowSize размер окна истории (например, 300 или 500)
     */
    public static EnsemblePredictor createDefaultEnsemble(int windowSize) {
        return new EnsemblePredictor(List.of(
                new EfficientCountPredictor(windowSize),
                new MultiOrderMarkovPredictor(windowSize),
                new PatternPredictor(windowSize),
                new SlidingWindowFreqPredictor(windowSize)
        ));
    }

    // Перегрузка для совместимости
    public static EnsemblePredictor createDefaultEnsemble() {
        return createDefaultEnsemble(500);
    }

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        EnumMap<Spot, Double> aggregatedProbabilities = new EnumMap<>(Spot.class);
        int activePredictors = 0;

        for (Predictor predictor : predictors) {
            List<SpotPrediction> predictions = predictor.predictSpot(state, config);

            // Если предиктор вернул пустой список (недостаточно данных), пропускаем его
            if (!predictions.isEmpty()) {
                activePredictors++;
                for (SpotPrediction sp : predictions) {
                    aggregatedProbabilities.merge(sp.spot(), sp.probability(), Double::sum);
                }
            }
        }

        if (activePredictors == 0 || aggregatedProbabilities.isEmpty()) {
            return List.of();
        }

        final int count = activePredictors;

        // 1. Усредняем вероятности (сумма / количество активных предикторов)
        // 2. Нормализуем, чтобы сумма всех вероятностей была равна 1.0
        double totalSum = aggregatedProbabilities.values().stream().mapToDouble(v -> v).sum();

        if (totalSum == 0) return List.of();

        return aggregatedProbabilities.entrySet().stream()
                .map(entry -> {
                    // (Value / count) / (totalSum / count) = Value / totalSum
                    // Упрощаем математику для нормализации
                    return SpotPrediction.of(entry.getKey(), entry.getValue() / totalSum);
                })
                .sorted((p1, p2) -> Double.compare(p2.probability(), p1.probability())) // Сортировка DESC
                .toList();
    }

    @Override
    public List<BetTypePrediction> predictType(State state, Config config) {
        // Логика аналогична predictSpot
        EnumMap<BetType, Double> enumMap = new EnumMap<>(BetType.class);
        int active = 0;

        for (Predictor predictor : predictors) {
            List<BetTypePrediction> list = predictor.predictType(state, config);
            if (!list.isEmpty()) {
                active++;
                list.forEach(btp -> enumMap.merge(btp.type(), btp.probability(), Double::sum));
            }
        }

        if (active == 0 || enumMap.isEmpty()) return List.of();

        double totalSum = enumMap.values().stream().mapToDouble(v -> v).sum();
        if (totalSum == 0) return List.of();

        return enumMap.entrySet().stream()
                .map(entry -> BetTypePrediction.of(entry.getKey(), entry.getValue() / totalSum))
                .sorted((p1, p2) -> Double.compare(p2.probability(), p1.probability()))
                .toList();
    }

    @Override
    public ColorPrediction predictColor(State state, Config config) {
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