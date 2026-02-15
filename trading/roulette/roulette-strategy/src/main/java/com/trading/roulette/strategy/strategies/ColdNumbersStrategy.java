package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.utils.BetHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Strategy(tags = {Tag.STATISTICAL})
public class ColdNumbersStrategy extends AbstractStrategy {

    private final int numberOfColdSpots;

    public ColdNumbersStrategy(Config config) {
        super(config);
        numberOfColdSpots = 5;
    }

    @Override
    public List<Bet> decide() {
        if (getMarket().getHistory().isEmpty()) return List.of();

        Map<Spot, Integer> frequencies = getMarket().getFrequencies();

        List<Spot> spots = Spot.getAvailableList(getConfig().rouletteType()).stream()
                .sorted(Comparator.comparingInt(spot -> frequencies.getOrDefault(spot, 0)))
                .limit(numberOfColdSpots)
                .toList();

        long minBet = getMinBet();

        return spots.isEmpty() ? List.of() :
                spots.stream()
                        .map(spot -> Bet.of(BetHelper.straightUp(spot), minBet))
                        .toList();
    }
}