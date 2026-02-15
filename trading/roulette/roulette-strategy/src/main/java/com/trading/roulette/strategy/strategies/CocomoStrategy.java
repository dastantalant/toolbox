package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;

import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class CocomoStrategy extends AbstractStrategy {

    private final BetType type;
    private long secondBetValue;

    public CocomoStrategy(Config config) {
        super(config);
        type = BetType.FIRST_DOZEN;
    }

    public List<Bet> decide() {
        long lastValue = getLastBet();
        long nextValue = secondBetValue + lastValue;
        secondBetValue = lastValue;
        long value = isLastBetWon() ? getMinBet()
                : Math.clamp(nextValue, getMinBet(), getMaxBet());
        return Bet.list(type, value);
    }
}