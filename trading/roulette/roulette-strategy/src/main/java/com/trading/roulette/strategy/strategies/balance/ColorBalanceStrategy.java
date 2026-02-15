package com.trading.roulette.strategy.strategies.balance;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

@Strategy(tags = {Tag.STATISTICAL})
public class ColorBalanceStrategy extends AbstractBalanceStrategy {

    private final double balanceThreshold;

    public ColorBalanceStrategy(Config config) {
        super(config);
        balanceThreshold = 0.45;
    }

    @Override
    public List<Bet> decide() {
        return processBet(BetType.RED, getState(), () -> {
            double red = getState().getMarket().getRedRate();
            double black = getState().getMarket().getBlackRate();
            double total = red + black;
            BetType result = BetType.RED;
            if (total > 0 && black < total * balanceThreshold) {
                result = BetType.BLACK;
            }
            return result;
        });
    }

}