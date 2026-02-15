package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.strategy.BettingHelper;

import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class FibonacciStrategy extends AbstractStrategy {

    private long prev;
    private long current;

    public FibonacciStrategy(Config config) {
        super(config);
        resetSequence();
    }

    @Override
    public List<Bet> decide() {
        if (isLastBet()) {
            if (isLastBetWon()) {
                long newPrev = Math.max(0, prev - (current - prev));
                long newCurrent = prev;
                prev = newPrev;
                current = newCurrent;
                if (current <= 0) {
                    resetSequence();
                }
            } else {
                long newCurrent = prev + current;
                prev = current;
                current = newCurrent;
            }
        }

        long betMultiplier = current > 0 ? current : 1;
        long totalBetAmount = getMinBet() * betMultiplier;
        long safeBetAmount = Math.min(totalBetAmount, getVirtualBalance());

        if (safeBetAmount < getMinBet()) {
            return List.of();
        }
        return BettingHelper.createDiversifiedBets(safeBetAmount, getState(), getConfig());
    }

    private void resetSequence() {
        prev = 0;
        current = 1;
    }
}