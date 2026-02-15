package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.utils.BetHelper;
import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.trading.roulette.api.enums.Spot.*;

@Strategy(tags = {Tag.PHYSICAL})
public class NeighborStrategy extends AbstractStrategy {

    private final double minProbabilityThreshold;
    private final Map<Integer, Set<Spot>> groups;
    private final Predictor predictor;

    public NeighborStrategy(Config config) {
        super(config);
        minProbabilityThreshold = 0.1;
        predictor = new MultiOrderMarkovPredictor();
        groups = Map.of(1, EnumSet.of(SPOT_32, SPOT_15, SPOT_19, SPOT_04, SPOT_21, SPOT_02, SPOT_25, SPOT_17, SPOT_34),
                2, EnumSet.of(SPOT_06, SPOT_27, SPOT_13, SPOT_36, SPOT_11, SPOT_30, SPOT_08, SPOT_23, SPOT_10),
                3, EnumSet.of(SPOT_05, SPOT_24, SPOT_16, SPOT_33, SPOT_01, SPOT_20, SPOT_14, SPOT_31, SPOT_09),
                4, EnumSet.of(SPOT_22, SPOT_18, SPOT_29, SPOT_07, SPOT_28, SPOT_12, SPOT_35, SPOT_03, SPOT_26, SPOT_0));
    }

    @Override
    public List<Bet> decide() {
        // 1. Убираем проверку на European
        // if (getConfig().rouletteType() != RouletteType.EUROPEAN) return List.of();

        // 2. Логика для Американки сложнее, так как стандартных "секторов" (Voisins, Orphelins) там нет.
        // Но если мы хотим просто ставить на соседей по прогнозу Markov:

        Map<Spot, Double> spotProbs = new HashMap<>();
        for (SpotPrediction prediction : predictor.predictSpot(getState(), getConfig())) {
            spotProbs.put(prediction.spot(), prediction.probability());
        }

        // Находим лучший Spot
        var bestSpotOpt = spotProbs.entrySet().stream().max(Map.Entry.comparingByValue());
        if (bestSpotOpt.isEmpty() || bestSpotOpt.get().getValue() < minProbabilityThreshold) {
            return List.of();
        }

        Spot target = bestSpotOpt.get().getKey();

        // 3. Берем соседей физически по колесу (код уже умеет это делать через Spot.getWheelLayouts)
        // Реализуем простую ставку: Число + 2 соседа слева + 2 справа
        return createNeighborBets(target, 2);
    }

    // Добавь этот вспомогательный метод
    private List<Bet> createNeighborBets(Spot center, int radius) {
        List<Bet> bets = new ArrayList<>();
        long val = getMinBet();
        RouletteType type = getConfig().rouletteType(); // Тут придет AMERICAN
        Spot[] layout = Spot.wheelLayouts().get(type); // В Spot.java уже прописан порядок для AMERICAN

        if (layout == null) return List.of();

        int centerPos = center.wheelPosition(type);
        if (centerPos == -1) return List.of();

        int len = layout.length;
        for (int i = -radius; i <= radius; i++) {
            int idx = (centerPos + i + len) % len;
            bets.add(Bet.of(BetHelper.straightUp(layout[idx]), val));
        }
        return bets;
    }
}