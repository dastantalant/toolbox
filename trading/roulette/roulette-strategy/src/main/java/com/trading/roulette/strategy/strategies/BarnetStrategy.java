package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;

import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class BarnetStrategy extends AbstractStrategy {
    private final BetType type = BetType.RED;
    private int count;

    public BarnetStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        count = isLastBetWon() ? (count + 1) : 0;

        long minBet = getMinBet();

        return switch (count % 4) {
            case 0 -> Bet.list(type, minBet);
            case 1 -> Bet.list(type, minBet * 3);
            case 2 -> Bet.list(type, minBet * 2);
            case 3 -> Bet.list(type, minBet * 6);
            default -> throw new IllegalArgumentException();
        };
    }
}