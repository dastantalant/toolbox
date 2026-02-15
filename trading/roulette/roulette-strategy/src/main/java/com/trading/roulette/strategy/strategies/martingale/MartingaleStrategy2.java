package com.trading.roulette.strategy.strategies.martingale;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

import static com.trading.roulette.api.enums.BetType.BLACK;
import static com.trading.roulette.api.enums.BetType.RED;

@Strategy(tags = {Tag.PROGRESSIVE})
public class MartingaleStrategy2 extends AbstractMartingaleStrategy {

    public MartingaleStrategy2(Config config) {
        super(config, List.of());
    }

    @Override
    public BetType getNextBetType() {
        return getState().getMarket().getBlackRate() <= getState().getMarket().getRedRate() ? RED : BLACK;
    }
}