package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;

import java.util.List;

import static com.trading.roulette.api.enums.BetType.*;
import static com.trading.roulette.api.model.Bet.of;

@Strategy(tags = {Tag.COVERAGE})
public class TripleSixStrategy extends AbstractStrategy {

    public TripleSixStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        return List.of(of(RED, getMinBet() * 36),
                of(SPLIT_0_2, getMinBet() * 4),
                of(SPLIT_8_11, getMinBet() * 4),
                of(SPLIT_10_13, getMinBet() * 4),
                of(SPLIT_17_20, getMinBet() * 4),
                of(SPLIT_26_29, getMinBet() * 4),
                of(SPLIT_28_31, getMinBet() * 4),
                of(SPOT_04, getMinBet() * 2),
                of(SPOT_06, getMinBet() * 2),
                of(SPOT_15, getMinBet() * 2));
    }
}