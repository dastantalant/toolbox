package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class OscarsGrindStrategy extends AbstractStrategy {
    private final BetType type = BetType.RED;
    private long currentUnit;
    private long cycleTargetBalance;

    public OscarsGrindStrategy(Config config) {
        super(config);
        resetCycle(config.startBalance());
    }

    private void resetCycle(long currentBalance) {
        currentUnit = getMinBet();
        cycleTargetBalance = currentBalance + currentUnit;
    }

    @Override
    public List<Bet> decide() {
        long balance = getVirtualBalance();

        if (isLastBet()) {
            if (balance >= cycleTargetBalance) {
                resetCycle(balance);
            } else {
                if (isLastBetWon()) {
                    long debt = cycleTargetBalance - balance;
                    currentUnit = Math.min(getLastBet() + getMinBet(), debt + getMinBet());
                    long nextPotential = getLastBet() + getMinBet();
                    long neededToClose = cycleTargetBalance - balance;
                    currentUnit = Math.min(nextPotential, neededToClose);
                } else {
                    currentUnit = getLastBet();
                }
            }
        } else {
            resetCycle(balance);
        }
        currentUnit = Math.clamp(currentUnit, getMinBet(), getMaxBet());
        currentUnit = Math.min(currentUnit, balance);

        return (currentUnit < getMinBet()) ? List.of() : Bet.list(type, currentUnit);
    }
}
