package com.trading.roulette.strategy.strategies.balance;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.State;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractBalanceStrategy extends AbstractStrategy {
    protected AbstractBalanceStrategy(Config config) {
        super(config);
    }

    protected List<Bet> processBet(BetType first, State state, Supplier<BetType> supplier) {
        if (getMarket().getHistory().isEmpty()) {
            return create(first, getMinBet(), state);
        }
        long value = (!isLastBet() || isLastBetWon()) ? getMinBet() : getLastBet() * 2;
        return create(supplier.get(), value, state);
    }

    protected List<Bet> create(BetType type, long value, State state) {
        long balance = getVirtualBalance();
        long safeValue = Math.min(value, balance);
        if (safeValue <= 0 && balance > 0) {
            safeValue = getMinBet();
        } else if (safeValue <= 0) {
            return List.of();
        }
        return Bet.list(type, safeValue);
    }
}
