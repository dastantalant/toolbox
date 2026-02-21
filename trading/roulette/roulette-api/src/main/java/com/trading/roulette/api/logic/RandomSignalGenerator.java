package com.trading.roulette.api.logic;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;

import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class RandomSignalGenerator implements SignalGenerator {
    private final RandomGenerator rng;
    private final List<Spot> availableSpots;

    public RandomSignalGenerator(Config config) {
        rng = RandomGeneratorFactory.getDefault().create(config.hashCode());
        availableSpots = Spot.availableList(config.rouletteType());
    }

    @Override
    public List<BetType> generate(State state) {
        if (availableSpots.isEmpty()) return Collections.emptyList();
        Spot randomSpot = availableSpots.get(rng.nextInt(availableSpots.size()));
        return List.of(BetType.straightUpBetFor(randomSpot));
    }
}