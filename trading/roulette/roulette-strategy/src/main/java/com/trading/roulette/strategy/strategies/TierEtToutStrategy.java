package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class TierEtToutStrategy extends AbstractStrategy {
    private long bankroll;
    private boolean stageTwo;

    public TierEtToutStrategy(Config config) {
        super(config);
        this.bankroll = config.startBalance();
        this.stageTwo = false;
    }

    @Override
    public List<Bet> decide() {
        if (isLastBet()) {
            if (isLastBetWon()) {
                bankroll = getVirtualBalance();
                stageTwo = false;
            } else {
                if (!stageTwo) {
                    stageTwo = true;
                } else {
                    bankroll = getVirtualBalance();
                    stageTwo = false;
                }
            }
        }

        long betValue;
        long part1 = (long) Math.floor(bankroll / 3.0);

        if (!stageTwo) {
            betValue = part1;
        } else {
            betValue = bankroll - part1;
        }

        if (betValue < getMinBet()) return List.of();

        return Bet.list(BetType.BLACK, betValue);
    }
}