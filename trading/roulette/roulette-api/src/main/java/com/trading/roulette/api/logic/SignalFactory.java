package com.trading.roulette.api.logic;


import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

public final class SignalFactory {
    private SignalFactory() {
    }

    public static SignalGenerator create(Config config) {
        return switch (config.signalType()) {
            case RED -> _ -> List.of(BetType.RED);
            case BLACK -> _ -> List.of(BetType.BLACK);
            case RANDOM -> new RandomSignalGenerator(config);
            case HOT_NUMBERS -> new HotNumbersSignal(3);
            case COLD_NUMBERS -> new HotNumbersSignal(3, true);
            case NONE -> _ -> List.of();
            default -> throw new IllegalArgumentException("Unsupported signal type: " + config.signalType());
        };
    }
}