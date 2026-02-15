package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.BetTypePrediction;
import com.trading.roulette.api.model.ColorPrediction;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.utils.BetHelper;

import java.util.Deque;
import java.util.EnumMap;
import java.util.List;

public class MarkovPredictor2 implements Predictor {

    private final int min;
    private final double probability;
    private final EnumMap<Spot, EnumMap<Spot, Long>> spotMap;
    private final EnumMap<BetType, EnumMap<BetType, Long>> typeMap;
    private long transitions;

    public MarkovPredictor2() {
        spotMap = new EnumMap<>(Spot.class);
        typeMap = new EnumMap<>(BetType.class);
        min = 2;
        probability = 0.0;
        transitions = 0;
    }

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        if (state.getMarket().getHistory().size() < min) {
            return List.of();
        }

        updateMarkovModel(state, config);
        EnumMap<Spot, Long> enumMap = spotMap.getOrDefault(state.getMarket().getLastSpot(), new EnumMap<>(Spot.class));

        return Spot.getAvailableList(config.rouletteType())
                .stream()
                .map(spot -> SpotPrediction.of(spot, calculate(enumMap.getOrDefault(spot, 0L))))
                .filter(prediction -> prediction.probability() > 0)
                .toList();
    }

    @Override
    public List<BetTypePrediction> predictType(State state, Config config) {
        if (state.getMarket().getHistory().size() < min) {
            return List.of();
        }

        updateMarkovModel(state, config);
        List<BetType> list = BetType.getAvailableList(config.rouletteType());
        List<BetType> filtered = list.stream()
                .filter(type -> BetHelper.isWin(type, state.getMarket().getLastSpot()))
                .toList();

        if (filtered.isEmpty()) {
            return List.of();
        }

        return list.stream()
                .flatMap(item -> filtered.stream()
                        .map(next -> BetTypePrediction.of(next,
                                calculate(typeMap.getOrDefault(item, new EnumMap<>(BetType.class)).getOrDefault(next, 0L))))
                        .filter(prediction -> prediction.probability() > 0))
                .toList();
    }

    @Override
    public ColorPrediction predictColor(State state, Config config) {
        return ColorPrediction.of(probability);
    }

    private void updateMarkovModel(State state, Config config) {
        Deque<Spot> history = state.getMarket().getHistory();
        if (history.size() < min) {
            return;
        }
        var iterator = history.iterator();
        if (!iterator.hasNext()) return;
        Spot prev = iterator.next();
        while (iterator.hasNext()) {
            Spot curr = iterator.next();
            spotMap.computeIfAbsent(prev, k -> new EnumMap<>(Spot.class))
                    .merge(curr, 1L, Long::sum);
            Spot finalPrev = prev;
            List<BetType> wins1 = BetType.getAvailableList(config.rouletteType())
                    .stream()
                    .filter(type -> BetHelper.isWin(type, finalPrev))
                    .toList();
            List<BetType> wins2 = BetType.getAvailableList(config.rouletteType())
                    .stream()
                    .filter(type -> BetHelper.isWin(type, curr))
                    .toList();
            for (BetType t1 : wins1) {
                for (BetType t2 : wins2) {
                    typeMap.computeIfAbsent(t1, k -> new EnumMap<>(BetType.class))
                            .merge(t2, 1L, Long::sum);
                }
            }
            prev = curr;
        }
        transitions = history.size() - 1L;
    }

    private double calculate(long occurrences) {
        return transitions == 0 ? probability : (double) occurrences / transitions;
    }

    public void reset() {
        spotMap.clear();
        typeMap.clear();
        transitions = 0;
    }
}