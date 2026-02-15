package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.utils.HistoryUtil;

import java.util.*;

public class MultiOrderMarkovPredictor implements Predictor {
    private final double weightOrder1 = 0.35;
    private final double weightOrder2 = 0.65;
    private final int minHistory = 3;
    private final int windowSize;

    public MultiOrderMarkovPredictor() {
        this(500);
    }

    public MultiOrderMarkovPredictor(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        List<Spot> window = HistoryUtil.window(state, windowSize);
        if (window.size() < minHistory) {
            return List.of();
        }

        // Строим локальную модель на основе окна
        Map<Spot, Map<Spot, Long>> order1Map = new HashMap<>();
        Map<String, Map<Spot, Long>> order2Map = new HashMap<>();

        // Заполняем модель данными из окна
        for (int i = 2; i < window.size(); i++) {
            Spot s1 = window.get(i - 2);
            Spot s2 = window.get(i - 1);
            Spot target = window.get(i);

            // Order 1: s2 -> target
            order1Map.computeIfAbsent(s2, k -> new EnumMap<>(Spot.class))
                    .merge(target, 1L, Long::sum);

            // Order 2: (s1, s2) -> target
            String key = s1.name() + "|" + s2.name();
            order2Map.computeIfAbsent(key, k -> new EnumMap<>(Spot.class))
                    .merge(target, 1L, Long::sum);
        }

        // Предсказываем следующий ход
        Spot lastSpot = window.getLast();
        Spot prevSpot = window.get(window.size() - 2);

        Map<Spot, Double> scores = new EnumMap<>(Spot.class);

        // Анализ Order 1
        Map<Spot, Long> o1Candidates = order1Map.getOrDefault(lastSpot, Map.of());
        double o1Total = o1Candidates.values().stream().mapToLong(l -> l).sum();
        if (o1Total > 0) {
            o1Candidates.forEach((spot, count) ->
                    scores.merge(spot, (count / o1Total) * weightOrder1, Double::sum));
        }

        // Анализ Order 2
        String keyOrder2 = prevSpot.name() + "|" + lastSpot.name();
        Map<Spot, Long> o2Candidates = order2Map.getOrDefault(keyOrder2, Map.of());
        double o2Total = o2Candidates.values().stream().mapToLong(l -> l).sum();
        if (o2Total > 0) {
            o2Candidates.forEach((spot, count) ->
                    scores.merge(spot, (count / o2Total) * weightOrder2, Double::sum));
        }

        if (scores.isEmpty()) return List.of();
        return normalize(scores);
    }

    private List<SpotPrediction> normalize(Map<Spot, Double> scores) {
        double sum = scores.values().stream().mapToDouble(v -> v).sum();
        if (sum == 0) return List.of();

        return scores.entrySet().stream()
                .map(e -> SpotPrediction.of(e.getKey(), e.getValue() / sum))
                .sorted(Comparator.comparingDouble(SpotPrediction::probability).reversed())
                .toList();
    }
}