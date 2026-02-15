package com.trading.roulette.strategy.strategies.martingale;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;

@Strategy(tags = {Tag.PROGRESSIVE})
public class EastCoastProgressionStrategy extends AbstractMartingaleStrategy {

    public EastCoastProgressionStrategy(Config config) {
        super(config, () -> config.minBet() * 2);

    }

    @Override
    protected long calculate() {
        long balance = getVirtualBalance();
        long startBalance = getStartBalance();
        return (isLastBetWon() && balance > startBalance)
                ? Math.max((balance - startBalance) / 2, getMinBet())
                : supplier.getAsLong();
    }
}