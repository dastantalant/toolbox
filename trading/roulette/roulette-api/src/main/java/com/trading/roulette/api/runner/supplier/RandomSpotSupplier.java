package com.trading.roulette.api.runner.supplier;

import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.enums.GenerateType;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class RandomSpotSupplier extends BaseSpotSupplier {

    private final List<Spot> pool;
    private final GenerateType generateType;
    private final RandomGenerator rng;

    public RandomSpotSupplier(RouletteType rouletteType, GenerateType generateType, long maxGenerations) {
        this(rouletteType, generateType, maxGenerations, 12345);
    }
    public RandomSpotSupplier(RouletteType rouletteType, GenerateType generateType, long maxGenerations, long seed) {
        super(rouletteType, maxGenerations);
        this.generateType = generateType;
        rng = RandomGeneratorFactory.of("L128X256MixRandom").create(seed);
        List<Spot> allSpots = Spot.availableList(rouletteType);
        pool = switch (generateType) {
            case RANDOM, ROTATION_NUMBER, ROTATION_WHEEL -> allSpots;
            case RANDOM_RED_ONLY -> allSpots.stream().filter(Spot::isRed).toList();
            case RANDOM_BLACK_ONLY -> allSpots.stream().filter(Spot::isBlack).toList();
            case RANDOM_EXCEPT_ONE -> allSpots.stream().filter(spot -> spot != Spot.SPOT_01).toList();
        };
        if (pool.isEmpty()) {
            throw new IllegalArgumentException("Spot pool is empty for generation type: " + generateType);
        }
    }
    public static SpotSupplier of(RouletteType rouletteType, GenerateType generateType, long maxGenerations, long seed) {
        return new RandomSpotSupplier(rouletteType, generateType, maxGenerations, seed);
    }

    public static SpotSupplier of(RouletteType rouletteType, GenerateType generateType, long maxGenerations) {
        return new RandomSpotSupplier(rouletteType, generateType, maxGenerations);
    }

    public Spot getInternal() {
        return pool.get(rng.nextInt(pool.size()));
    }

    public GenerateType getGenerateType() {
        return generateType;
    }
}