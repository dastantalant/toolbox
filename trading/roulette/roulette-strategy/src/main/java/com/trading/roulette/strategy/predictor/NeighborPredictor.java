package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.Predictor;

import java.util.ArrayList;
import java.util.List;

public class NeighborPredictor implements Predictor {
    private final int neighborCount; // Сколько соседей с каждой стороны (обычно 2)

    public NeighborPredictor(int neighborCount) {
        this.neighborCount = neighborCount;
    }

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        Spot lastSpot = state.getMarket().getLastSpot();
        if (lastSpot == null) return List.of();

        // Получаем раскладку колеса (Европа/Америка)
        Spot[] layout = Spot.wheelLayouts().get(config.rouletteType());
        if (layout == null) return List.of();

        int wheelSize = layout.length;
        int lastPos = lastSpot.wheelPosition(config.rouletteType());

        if (lastPos == -1) return List.of();

        List<SpotPrediction> predictions = new ArrayList<>();

        // Берем сектор вокруг последнего числа
        // i идет от -2 до +2 (если neighborCount = 2)
        for (int i = -neighborCount; i <= neighborCount; i++) {
            // Вычисляем индекс с учетом зацикливания колеса
            int index = (lastPos + i + wheelSize) % wheelSize;
            Spot neighbor = layout[index];

            // Вероятность распределяем: центр (последнее число) чуть выше, края чуть ниже
            // Или равномерно. Здесь сделаем равномерно для простоты = 1.0
            // Ensemble потом сам нормализует это.
            predictions.add(SpotPrediction.of(neighbor, 1.0));
        }

        return predictions;
    }
}