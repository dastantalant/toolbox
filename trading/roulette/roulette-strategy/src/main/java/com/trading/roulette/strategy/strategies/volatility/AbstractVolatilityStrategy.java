package com.trading.roulette.strategy.strategies.volatility;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

public abstract class AbstractVolatilityStrategy extends AbstractStrategy {
    protected final BetType type;

    protected AbstractVolatilityStrategy(Config config) {
        super(config);
        type = BetType.RED;
    }

    protected List<Bet> create(BetType type, long value) {
        long minBet = getMinBet();
        long safeBalance = Math.max(0, getVirtualBalance());

        if (safeBalance < minBet) {
            return List.of();
        }

        double upper = Math.min(getMaxBet(), safeBalance);

        long clamped = (long) Math.clamp(value, minBet, upper);

        return (clamped < minBet) ? List.of() : Bet.list(type, clamped);
    }
}
