package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.AbstractStrategy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class TenUnitStrategy extends AbstractStrategy {

    private final BetType type;
    private final int count;
    private final double fraction;
    private final int multiplayer;

    private final Deque<Integer> numbers;
    private long setStartBalance;
    private long limitValue;
    private boolean useLastBetLeftNumber;
    private boolean useLastBetAllLimit;
    private int lastBetMultiplier;
    private int lastRemainingLimitMultiplier;

    public TenUnitStrategy(Config config) {
        super(config);
        type = BetType.RED;
        count = 10;
        fraction = 0.1;
        multiplayer = 1;
        numbers = new ArrayDeque<>(count);
        setStartBalance = Math.max(getVirtualBalance(), config.startBalance());
        limitValue = (long) (setStartBalance * fraction);
    }

    @Override
    public List<Bet> decide() {
        if (getRemainingLimit() <= 0) {
            initializeSet();
        }

        if (isLastBetWon()) {
            if (!useLastBetLeftNumber && !useLastBetAllLimit) {
                if (!numbers.isEmpty()) numbers.removeFirst();
                if (!numbers.isEmpty()) numbers.removeLast();
            } else if (useLastBetAllLimit) {
                if (!numbers.isEmpty()) numbers.removeFirst();
                numbers.addFirst(lastBetMultiplier);
            }
        } else if (isLastBet()) {
            if (useLastBetAllLimit) {
                numbers.clear();
            } else if (useLastBetLeftNumber) {
                if (!numbers.isEmpty()) numbers.removeFirst();
                numbers.addFirst(lastRemainingLimitMultiplier);
            } else {
                numbers.addLast(lastBetMultiplier);
            }
        }

        if (numbers.isEmpty()) {
            for (int i = 0; i < count; i++) {
                numbers.addLast(multiplayer);
            }
            initializeSet();
        }

        int multi = numbers.getFirst() + numbers.getLast();
        lastRemainingLimitMultiplier = (int) (getRemainingLimit() / getMinBet());
        if (getRemainingLimit() < getMinBet() * multi) {
            if (getMinBet() * numbers.getFirst() < getRemainingLimit()) {
                multi = numbers.getFirst();
                useLastBetLeftNumber = true;
            } else {
                multi = lastRemainingLimitMultiplier;
                useLastBetAllLimit = true;
            }
        }

        lastBetMultiplier = Math.max(multi, 1);
        return Bet.list(type, getMinBet() * lastBetMultiplier);
    }

    private void initializeSet() {
        setStartBalance = Math.max(getVirtualBalance(), getConfig().startBalance());
        limitValue = (long) (setStartBalance * fraction);
    }

    private long getRemainingLimit() {
        return limitValue - (setStartBalance - getVirtualBalance());
    }
}