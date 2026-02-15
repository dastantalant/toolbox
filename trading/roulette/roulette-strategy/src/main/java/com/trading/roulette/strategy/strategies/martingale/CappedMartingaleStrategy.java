package com.trading.roulette.strategy.strategies.martingale;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class CappedMartingaleStrategy extends AbstractMartingaleStrategy {

    private final int maxLossStreak;
    private int lossStreak;

    public CappedMartingaleStrategy(Config config) {
        maxLossStreak = 5;
        lossStreak = 0;
        super(config);
    }

    @Override
    public List<Bet> decide() {
        if (isLastBet()) {
            lossStreak = isLastBetWon() ? 0 : lossStreak + 1;
        }
        if (lossStreak >= maxLossStreak || !isLastBet() || isLastBetWon()) {
            lossStreak = (lossStreak >= maxLossStreak) ? 0 : lossStreak;
        }
        return create();
    }
}