package com.trading.roulette.strategy.strategies.martingale;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;

@Strategy(tags = {Tag.PROGRESSIVE})
public class GrandMartingaleStrategy extends AbstractMartingaleStrategy {

    public GrandMartingaleStrategy(Config config) {
        super(config);
    }

    @Override
    protected long calculateNext(Config config) {
        long lastBet = getLastBet();
        long idealNextBet = lastBet * 2;

        long currentMax = getMaxBet();
        long currentMin = getMinBet();
        if (idealNextBet > currentMax) {
            return currentMin;
        }
        return Math.max(idealNextBet, currentMin);
    }
}