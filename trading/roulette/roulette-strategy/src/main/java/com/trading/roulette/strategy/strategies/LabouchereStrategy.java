package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class LabouchereStrategy extends AbstractStrategy {
    private final BetType type = BetType.RED;
    private final Deque<Long> sequence = new LinkedList<>();
    private final long unit;

    public LabouchereStrategy(Config config) {
        super(config);
        unit = Math.max(1, config.minBet());
        resetSequence();
    }

    private void resetSequence() {
        sequence.clear();
        sequence.add(unit);
        sequence.add(unit * 2);
        sequence.add(unit * 3);
        sequence.add(unit * 4);
    }

    @Override
    public List<Bet> decide() {
        if (isLastBet()) {
            if (isLastBetWon()) {
                if (!sequence.isEmpty()) sequence.removeFirst();
                if (!sequence.isEmpty()) sequence.removeLast();
            } else {
                long lostValue = getLastBet();
                sequence.addLast(lostValue);
            }
        }

        if (sequence.isEmpty()) {
            resetSequence();
        }

        long betValue;
        if (sequence.size() == 1) {
            betValue = sequence.getFirst();
        } else {
            betValue = sequence.getFirst() + sequence.getLast();
        }

        betValue = Math.min(betValue, getMaxBet());
        betValue = Math.min(betValue, getVirtualBalance());

        return (betValue < getMinBet()) ? List.of() : Bet.list(type, betValue);
    }
}