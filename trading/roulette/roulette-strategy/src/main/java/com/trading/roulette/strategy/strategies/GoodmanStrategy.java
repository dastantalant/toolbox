package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.AbstractStrategy;

import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class GoodmanStrategy extends AbstractStrategy {

    private final BetType type;
    private int count;

    public GoodmanStrategy(Config config) {
        super(config);
        type = BetType.RED;
    }

    @Override
    public List<Bet> decide() {
        count = isLastBetWon() ? (count + 1) : 0;
        return switch (count) {
            case 0 -> Bet.list(type, getMinBet());
            case 1 -> Bet.list(type, getMinBet() * 2);
            case 2 -> Bet.list(type, getMinBet() * 3);
            default -> Bet.list(type, getMinBet() * 5);
        };
    }
}