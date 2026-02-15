package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;

import java.util.LinkedList;
import java.util.List;

@Strategy
public class MonteCarloStrategy extends AbstractStrategy {

    private final BetType type;
    private final LinkedList<Integer> numbers;

    public MonteCarloStrategy(Config config) {
        super(config);
        numbers = new LinkedList<>();
        type = BetType.FIRST_DOZEN;
    }

    @Override
    public List<Bet> decide() {
        if (numbers.isEmpty()) {
            numbers.addLast(1);
            numbers.addLast(2);
            numbers.addLast(3);
        }
        int multiplier = numbers.getFirst() + numbers.getLast();
        if (isLastBetWon()) {
            for (int i = 0; i < 2; i++) {
                if (!numbers.isEmpty()) {
                    numbers.removeFirst();
                }
            }
            for (int i = 0; i < 2; i++) {
                if (!numbers.isEmpty()) {
                    numbers.removeLast();
                }
            }
        } else if (isLastBet()) {
            numbers.addLast(multiplier);
        }

        if (numbers.size() <= 1) {
            numbers.clear();
            numbers.addLast(1);
            numbers.addLast(2);
            numbers.addLast(3);
        }
        return Bet.list(type, getMinBet() * multiplier);
    }
}