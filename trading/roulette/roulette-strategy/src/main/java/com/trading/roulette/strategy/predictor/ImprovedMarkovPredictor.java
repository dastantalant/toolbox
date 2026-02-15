package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.BetTypePrediction;
import com.trading.roulette.api.model.ColorPrediction;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.utils.BetHelper;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ImprovedMarkovPredictor implements Predictor {

    private final int size;
    private final Map<Spot, Map<Spot, Long>> spots;
    private final Map<BetType, Map<BetType, Long>> types;

    public ImprovedMarkovPredictor() {
        spots = new ConcurrentHashMap<>();
        types = new ConcurrentHashMap<>();
        size = 2;
    }

    public void updateModel(Spot[] lasts, RouletteType type) {
        if (lasts == null || lasts.length < 2) {
            return;
        }
        Spot spot1 = lasts[0];
        Spot spot2 = lasts[1];

        spots.computeIfAbsent(spot1, k -> new ConcurrentHashMap<>())
                .merge(spot2, 1L, Long::sum);

        List<BetType> types1 = findTypes(spot1, type);
        List<BetType> types2 = findTypes(spot2, type);

        for (BetType type1 : types1) {
            Map<BetType, Long> map = types.computeIfAbsent(type1, _ -> new ConcurrentHashMap<>());
            for (BetType type2 : types2) {
                map.merge(type2, 1L, Long::sum);
            }
        }
    }
    @Override
    public List<BetTypePrediction> predictType(State state, Config config) {
        Deque<Spot> history = state.getMarket().getHistory();
        if (history.size() < size) {
            return List.of();
        }

        var it = history.descendingIterator();
        Spot last  = it.next();
        Spot prev  = it.next();

        updateModel(new Spot[]{prev, last}, config.rouletteType());

        List<BetType> betTypes = findTypes(last, config.rouletteType());

        Map<BetType, Long> collect = betTypes.stream()
                .flatMap(lastType -> types.getOrDefault(lastType, Map.of()).entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));

        if (collect.isEmpty()) {
            return List.of();
        }

        final double sum = collect.values().stream().mapToDouble(Long::doubleValue).sum();
        if (sum == 0) return List.of();

        return collect.entrySet().stream()
                .map(e -> BetTypePrediction.of(e.getKey(), e.getValue() / sum))
                .filter(p -> p.probability() > 0)
                .sorted((p1, p2) -> Double.compare(p2.probability(), p1.probability()))
                .toList();
    }

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        Deque<Spot> history = state.getMarket().getHistory();
        if (history.size() < size) {
            return List.of();
        }

        var it = history.descendingIterator();
        Spot last = it.next();
        Spot prev = it.next();

        updateModel(new Spot[]{prev, last}, config.rouletteType());

        Map<Spot, Long> map = spots.getOrDefault(last, Map.of());
        if (map.isEmpty()) {
            return List.of();
        }

        final double sum = map.values().stream().mapToDouble(Long::doubleValue).sum();
        if (sum == 0) return List.of();

        return map.entrySet().stream()
                .map(e -> SpotPrediction.of(e.getKey(), e.getValue() / sum))
                .sorted((p1, p2) -> Double.compare(p2.probability(), p1.probability()))
                .toList();
    }


    @Override
    public ColorPrediction predictColor(State state, Config config) {
        List<SpotPrediction> list = predictSpot(state, config);

        if (list.isEmpty()) {
            return ColorPrediction.of();
        }

        double red = list.stream()
                .filter(item -> item.spot().isRed())
                .mapToDouble(SpotPrediction::probability)
                .sum();
        double black = list.stream()
                .filter(item -> item.spot().isBlack())
                .mapToDouble(SpotPrediction::probability)
                .sum();
        double green = list.stream()
                .filter(item -> item.spot().isGreen())
                .mapToDouble(SpotPrediction::probability)
                .sum();

        return ColorPrediction.of(red, black, green);
    }

    private List<BetType> findTypes(Spot spot, RouletteType rouletteType) {
        return BetType.availableList(rouletteType).stream()
                .filter(type -> BetHelper.isWin(type, spot))
                .toList();
    }
}
