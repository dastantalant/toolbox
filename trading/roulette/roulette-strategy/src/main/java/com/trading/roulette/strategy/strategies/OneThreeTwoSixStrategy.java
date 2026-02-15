package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

@Strategy
public class OneThreeTwoSixStrategy extends AbstractStrategy {
    private final BetType type = BetType.BLACK;
    private int level;

    public OneThreeTwoSixStrategy(Config config) {
        super(config);
        level = 0;
    }

    @Override
    public List<Bet> decide() {
        if (isLastBet()) {
            if (isLastBetWon()) {
                level++;
                if (level > 3) {
                    level = 0;
                }
            } else {
                level = 0;
            }
        }

        long base = getMinBet();
        long multiplier = switch (level) {
            case 1 -> 3;
            case 2 -> 2;
            case 3 -> 6;
            default -> 1;
        };

        long amount = base * multiplier;
        amount = Math.min(amount, getVirtualBalance());

        return (amount < base) ? List.of() : Bet.list(type, amount);
    }
}