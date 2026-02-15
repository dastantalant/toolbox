package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.AbstractStrategy;

import java.util.List;

@Strategy(tags = {Tag.SPECIAL})
public class TenPercentStrategy extends AbstractStrategy {

    private final BetType type;
    private final double percentage;

    public TenPercentStrategy(Config config) {
        super(config);
        type = BetType.RED;
        percentage = 0.10;
    }

    @Override
    public List<Bet> decide() {
        long balance = getVirtualBalance();
        long value = Math.clamp((long) (balance * percentage), getMinBet(), getMaxBet());
        if (balance < value) {
            return List.of();
        }
        return Bet.list(type, value);
    }
}