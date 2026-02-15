
package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.AbstractStrategy;

import java.util.List;

@Strategy(tags = {Tag.COVERAGE})
public class RomanovskyStrategy extends AbstractStrategy {

    public RomanovskyStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        long value = getMinBet();
        return List.of(
                Bet.of(BetType.FIRST_DOZEN, value * 3),
                Bet.of(BetType.SECOND_DOZEN, value * 3),
                Bet.of(BetType.CORNER_1_2_4_5, value),
                Bet.of(BetType.CORNER_32_33_35_36, value));
    }
}