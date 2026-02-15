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
public class Rate9848PercentStrategy extends AbstractStrategy {

    public Rate9848PercentStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        Deque<Spot> history = getMarket().getHistory();
        if (history.size() < 3) return List.of();

        var it = history.descendingIterator();
        Spot s3 = it.next();
        Spot s2 = it.next();
        Spot s1 = it.next();

        if (s1.isFirstDozenOrZeroAndDoubleZero()
                && s2.isFirstDozenOrZeroAndDoubleZero()
                && s3.isFirstDozenOrZeroAndDoubleZero()) {
            var lastBets = getLastBets();
            long value = (lastBets != null && !lastBets.isEmpty())
                    ? lastBets.getFirst().value() * 2
                    : getMinBet();
            return Bet.of(BetType.SECOND_DOZEN, value, BetType.THIRD_DOZEN, value);
        }
        return List.of();
    }
}
