package com.trading.roulette.strategy.strategies;


import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.AbstractStrategy;

import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class ReverseMartingaleRedStrategy extends AbstractStrategy {

    private final BetType type;

    public ReverseMartingaleRedStrategy(Config config) {
        super(config);
        type = BetType.RED;
    }

    @Override
    public List<Bet> decide() {
        long last = getLastBet();
        boolean b = (isLastBetWon() && last < (getMinBet() * Math.pow(2, 5)));
        long value = b ? last * 2 : getMinBet();
        return Bet.list(type, value);
    }
}
