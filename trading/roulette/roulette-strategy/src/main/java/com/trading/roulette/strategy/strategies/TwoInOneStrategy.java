package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.AbstractStrategy;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class TwoInOneStrategy extends AbstractStrategy {

    private final BetType type;
    private final Deque<Integer> numbers;

    public TwoInOneStrategy(Config config) {
        super(config);
        numbers = new LinkedList<>();
        type = BetType.RED;
    }

    @Override
    public List<Bet> decide() {
        if (isLastBetWon()) {
            if (!numbers.isEmpty()) numbers.removeFirst();
            if (!numbers.isEmpty()) numbers.removeLast();
        } else if (isLastBet()) {
            numbers.addLast(numbers.size() <= 1 ? 1 : (numbers.getFirst() + numbers.getLast()));
        }
        long value = getMinBet() * (numbers.size() <= 1 ? 1 : numbers.getFirst() + numbers.getLast());
        return Bet.list(type, value);
    }
}