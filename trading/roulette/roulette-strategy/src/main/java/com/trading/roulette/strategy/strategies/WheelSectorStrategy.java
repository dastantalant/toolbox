package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.utils.BetHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Strategy(tags = {Tag.PHYSICAL})
public class WheelSectorStrategy extends AbstractStrategy {

    private final Predictor predictor;
    private final int size;
    private final double probability;

    public WheelSectorStrategy(Config config) {
        super(config);
        predictor = new MultiOrderMarkovPredictor();
        size = 5;
        probability = 0.2;
    }

    @Override
    public List<Bet> decide() {
        if (getConfig().rouletteType() == RouletteType.ONE_TO_36) {
            return List.of();
        }
        List<SpotPrediction> predictions = predictor.predictSpot(getState(), getConfig());
        if (predictions.isEmpty()) {
            return List.of();
        }

        SpotPrediction prediction = predictions.stream()
                .filter(p -> p.probability() >= probability)
                .max(Comparator.comparingDouble(SpotPrediction::probability))
                .orElse(null);

        if (prediction == null) {
            return List.of();
        }

        Set<Spot> sector = getSpotsInSector(prediction.spot(), size, getConfig().rouletteType());
        List<Bet> bets = new ArrayList<>(sector.size());
        long value = getMinBet();
        for (Spot spot : sector) {
            long sum = getVirtualBalance() - bets.stream()
                    .mapToLong(Bet::value)
                    .sum();
            if (sum < value) {
                break;
            }
            bets.add(Bet.of(BetHelper.straightUp(spot), value));
        }

        return bets;
    }

    private Set<Spot> getSpotsInSector(Spot centerSpot, int radius, RouletteType rouletteType) {
        Map<Spot, Integer> wheelPositions = Spot.WHEEL_POSITIONS_CACHE.get(rouletteType);
        Spot[] wheelLayout = Spot.wheelLayouts().get(rouletteType);

        if (wheelPositions == null || wheelLayout == null) {
            return Set.of();
        }

        int centerPos = wheelPositions.getOrDefault(centerSpot, -1);
        if (centerPos == -1) {
            return Set.of();
        }

        Set<Spot> sector = HashSet.newHashSet(radius * 2);
        int wheelSize = wheelLayout.length;

        for (int i = -radius; i <= radius; i++) {
            int currentPos = (centerPos + i + wheelSize) % wheelSize;
            sector.add(wheelLayout[currentPos]);
        }

        return sector;
    }
}