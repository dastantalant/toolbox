package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.Predictor;

import java.util.*;

/**
 * Предиктор "Импульса Сектора" (или Почерка Дилера).
 * <p>
 * Анализирует физическое расстояние (в слотах колеса) между последовательными выпадениями.
 * Если дилер бросает шарик ритмично, часто образуется пик в распределении смещений
 * (например, шарик падает через 10-15 слотов от предыдущего номера по часовой стрелке).
 */
public class SectorMomentumPredictor implements Predictor {

    private final int historyDepth;
    private final int sectorWidth;

    public SectorMomentumPredictor() {
        this.historyDepth = 50; // Анализируем последние 50 бросков
        this.sectorWidth = 2;   // Ищем пик +/- 2 слота (сектор из 5 чисел)
    }

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        Deque<Spot> history = state.getMarket().getHistory();
        if (history.size() < 2) return List.of();

        RouletteType type = config.rouletteType();
        Spot[] layout = Spot.wheelLayouts().get(type);
        if (layout == null) return List.of();

        int wheelSize = layout.length;
        Map<Integer, Double> distanceFrequency = new HashMap<>();

        var it = history.iterator();
        int skip = Math.max(0, history.size() - historyDepth - 1);
        for (int i = 0; i < skip && it.hasNext(); i++) {
            it.next();
        }

        if (!it.hasNext()) return List.of();

        Spot current = it.next();

        while (it.hasNext()) {
            Spot next = it.next();

            int pos1 = current.wheelPosition(type);
            int pos2 = next.wheelPosition(type);

            if (pos1 == -1 || pos2 == -1) {
                current = next;
                continue;
            }

            int distance = (pos2 - pos1 + wheelSize) % wheelSize;

            for (int offset = -1; offset <= 1; offset++) {
                int key = (distance + offset + wheelSize) % wheelSize;
                distanceFrequency.merge(key, 1.0 - (Math.abs(offset) * 0.3), Double::sum);
            }

            current = next;
        }

        if (distanceFrequency.isEmpty()) return List.of();

        int bestDistance = distanceFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        Spot lastSpot = state.getMarket().getLastSpot();
        int lastPos = lastSpot.wheelPosition(type);

        int targetPos = (lastPos + bestDistance) % wheelSize;

        List<SpotPrediction> predictions = new ArrayList<>();
        double totalWeight = 0;

        for (int i = -sectorWidth; i <= sectorWidth; i++) {
            int p = (targetPos + i + wheelSize) % wheelSize;
            Spot spot = layout[p];
            double prob = 1.0 - (Math.abs(i) * 0.15);
            predictions.add(SpotPrediction.of(spot, prob));
            totalWeight += prob;
        }

        final double finalWeight = totalWeight;
        return predictions.stream()
                .map(p -> SpotPrediction.of(p.spot(), p.probability() / finalWeight))
                .toList();
    }

}