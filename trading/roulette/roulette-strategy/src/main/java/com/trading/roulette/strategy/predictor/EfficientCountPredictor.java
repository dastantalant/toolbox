package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.BetTypePrediction;
import com.trading.roulette.api.model.ColorPrediction;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.utils.HistoryUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EfficientCountPredictor implements Predictor {

    private final int windowSize;

    public EfficientCountPredictor() {
        this(960);
    }

    public EfficientCountPredictor(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        List<Spot> window = HistoryUtil.window(state, windowSize);
        double size = window.size();
        if (size == 0) return List.of();

        Map<Spot, Long> frequencies = window.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return Spot.getAvailableList(config.rouletteType()).stream()
                .map(spot -> SpotPrediction.of(spot, frequencies.getOrDefault(spot, 0L) / size))
                .sorted(Comparator.comparingDouble(SpotPrediction::probability).reversed())
                .toList();
    }

    @Override
    public List<BetTypePrediction> predictType(State state, Config config) {
        List<Spot> window = HistoryUtil.window(state, windowSize);
        double size = window.size();
        if (size == 0) {
            return List.of();
        }

        // 1. Считаем частоты спотов внутри окна
        Map<Spot, Long> windowFrequencies = window.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<BetType> types = BetType.getAvailableList(config.rouletteType());
        EnumMap<BetType, Long> betMap = new EnumMap<>(BetType.class);

        // 2. Создаем карту маппинга Spot -> List<BetType>
        EnumMap<Spot, List<BetType>> spotToTypesMap = new EnumMap<>(Spot.class);
        for (BetType type : types) {
            for (Spot spot : type.getWinning()) {
                spotToTypesMap.computeIfAbsent(spot, _ -> new ArrayList<>()).add(type);
            }
        }

        // 3. Агрегируем данные только на основе частот окна
        for (Map.Entry<Spot, Long> entry : windowFrequencies.entrySet()) {
            List<BetType> winning = spotToTypesMap.get(entry.getKey());
            if (winning != null) {
                for (BetType type : winning) {
                    betMap.merge(type, entry.getValue(), Long::sum);
                }
            }
        }

        return types.stream()
                .map(type -> BetTypePrediction.of(type, calculate(betMap.getOrDefault(type, 0L), size)))
                .sorted(Comparator.comparingDouble(BetTypePrediction::probability).reversed())
                .toList();
    }

    @Override
    public ColorPrediction predictColor(State state, Config config) {
        List<Spot> window = HistoryUtil.window(state, windowSize);
        double size = window.size();
        if (size == 0) {
            return ColorPrediction.of();
        }

        long red = 0;
        long black = 0;
        long green = 0;

        for (Spot spot : window) {
            if (spot.isRed()) red++;
            else if (spot.isBlack()) black++;
            else if (spot.isGreen()) green++;
        }

        return ColorPrediction.of(calculate(red, size), calculate(black, size), calculate(green, size));
    }

    private double calculate(double occurrences, double totalCount) {
        return totalCount == 0 ? 0.0 : occurrences / totalCount;
    }
}