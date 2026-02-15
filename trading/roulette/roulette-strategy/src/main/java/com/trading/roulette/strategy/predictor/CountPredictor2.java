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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CountPredictor2 implements Predictor {

    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        Map<Spot, Long> countMap = getSpotTypeCountMap(state);
        double totalCount = state.getMarket().getHistory().size();
        return Spot.availableList(config.rouletteType())
                .stream()
                .map(spot -> SpotPrediction.of(spot, calculate(countMap.getOrDefault(spot, 0L), totalCount)))
                .toList();
    }

    @Override
    public List<BetTypePrediction> predictType(State state, Config config) {
        EnumMap<BetType, Long> countMap = getBetTypeCountMap(state, config);
        double totalCount = state.getMarket().getHistory().size();
        return BetType.availableList(config.rouletteType())
                .stream()
                .map(type -> BetTypePrediction.of(type, calculate(countMap.getOrDefault(type, 0L), totalCount)))
                .toList();
    }

    @Override
    public ColorPrediction predictColor(State state, Config config) {
        EnumMap<Spot, Long> countMap = getSpotTypeCountMap(state);
        List<Spot> availableSpots = Spot.availableList(config.rouletteType());
        if (availableSpots.isEmpty()) {
            return ColorPrediction.of();
        }

        double red = 0;
        double black = 0;
        double green = 0;
        for (Spot spot : availableSpots) {
            long spotCount = countMap.getOrDefault(spot, 0L);
            if (spot.isRed()) {
                red += spotCount;
            } else if (spot.isBlack()) {
                black += spotCount;
            } else if (spot.isGreen()) {
                green += spotCount;
            }
        }
        double totalCount = red + black + green;

        return ColorPrediction.of(calculate(red, totalCount), calculate(black, totalCount), calculate(green, totalCount));
    }

    private EnumMap<Spot, Long> getSpotTypeCountMap(State state) {
        EnumMap<Spot, Long> countMap = new EnumMap<>(Spot.class);
        for (Spot spot : state.getMarket().getHistory()) {
            countMap.merge(spot, 1L, Long::sum);
        }
        return countMap;
    }

    private EnumMap<BetType, Long> getBetTypeCountMap(State state, Config config) {
        EnumMap<BetType, Long> countMap = new EnumMap<>(BetType.class);
        List<BetType> availableTypes = BetType.availableList(config.rouletteType());
        for (Spot spot : state.getMarket().getHistory()) {
            for (BetType betType : availableTypes) {
                if (BetHelper.isWin(betType, spot)) {
                    countMap.merge(betType, 1L, Long::sum);
                }
            }
        }
        return countMap;
    }

    private double calculate(double occurrences, double totalCount) {
        return totalCount == 0 ? 0.0 : occurrences / totalCount;
    }
}