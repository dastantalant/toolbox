package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;

import java.util.Deque;
import java.util.List;

@Strategy(tags = {Tag.COVERAGE})
public class Rate9946PercentStrategy extends AbstractStrategy {

    public Rate9946PercentStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        Deque<Spot> history = getMarket().getHistory();
        if (history.size() < 4) return List.of();
        var it = history.descendingIterator();
        Spot s4 = it.next();
        Spot s3 = it.next();
        Spot s2 = it.next();
        Spot s1 = it.next();
        if (s1.isFirstDozenOrZeroAndDoubleZero()
                && s2.isFirstDozenOrZeroAndDoubleZero()
                && s3.isFirstDozenOrZeroAndDoubleZero()
                && s4.isFirstDozenOrZeroAndDoubleZero()) {
            return Bet.of(BetType.SECOND_DOZEN, getMinBet(), BetType.THIRD_DOZEN, getMinBet());
        }
        return List.of();
    }
}
