package com.trading.roulette.strategy.strategies.volatility;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.List;
import java.util.LongSummaryStatistics;

@Strategy(tags = {Tag.STATISTICAL})
public class SimpleAdaptiveVolatilityStrategy extends AbstractVolatilityStrategy {

    private final double lowVolatilityThresholdPercent;

    public SimpleAdaptiveVolatilityStrategy(Config config) {
        super(config);
        lowVolatilityThresholdPercent = 0.05;
    }

    @Override
    public List<Bet> decide() {
        var historyBalance = getHistoryBalance();
        if (historyBalance.isEmpty()) {
            return create(type, getMinBet());
        }

        LongSummaryStatistics stats = historyBalance.stream()
                .mapToLong(Long::longValue)
                .summaryStatistics();

        long range = stats.getMax() - stats.getMin();
        double multi = getStartBalance() * lowVolatilityThresholdPercent;

        if (range < multi) {
            return create(type, getMinBet());
        } else if (getVirtualBalance() > stats.getAverage()) {
            long nextBetValue = isLastBetWon() ? getLastBet() * 2 : getMinBet();
            return create(type, nextBetValue);
        }
        return create(type, getMinBet());
    }
}