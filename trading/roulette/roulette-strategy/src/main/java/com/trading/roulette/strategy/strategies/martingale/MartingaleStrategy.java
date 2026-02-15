package com.trading.roulette.strategy.strategies.martingale;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;

@Strategy(tags = {Tag.PROGRESSIVE})
public class MartingaleStrategy extends AbstractMartingaleStrategy {

    public MartingaleStrategy(Config config) {
        super(config);
    }
}