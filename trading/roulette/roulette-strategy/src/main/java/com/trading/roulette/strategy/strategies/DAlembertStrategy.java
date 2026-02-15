package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.strategy.BettingHelper;

import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class DAlembertStrategy extends AbstractStrategy {

    private int multiplier;

    public DAlembertStrategy(Config config) {
        super(config);
        multiplier = 1;
    }

    @Override
    public List<Bet> decide() {
        if (isLastBet()) {
            multiplier = isLastBetWon() ? Math.max(1, multiplier - 1) : multiplier + 1;
        }

        return BettingHelper.createDiversifiedBets(getMinBet() * multiplier, getState(), getConfig());
    }
}