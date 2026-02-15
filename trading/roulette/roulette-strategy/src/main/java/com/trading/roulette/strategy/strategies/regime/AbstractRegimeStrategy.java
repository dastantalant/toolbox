package com.trading.roulette.strategy.strategies.regime;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.Spot;

import java.util.List;

public abstract class AbstractRegimeStrategy extends AbstractStrategy {

    protected final int historyWindow;
    protected final double trendThreshold;
    protected final double volatilityLowThreshold;
    protected final int trendStreak;
    protected final int alternatingStreak;

    protected AbstractRegimeStrategy(Config config) {
        this(config, RegimeContext.of());
    }

    protected AbstractRegimeStrategy(Config config, RegimeContext rContext) {
        super(config);
        historyWindow = rContext.getHistoryWindow();
        trendThreshold = rContext.getTrendThreshold();
        volatilityLowThreshold = rContext.getVolatilityLowThreshold();
        trendStreak = rContext.getTrendStreak();
        alternatingStreak = rContext.getAlternatingStreak();
    }

    protected DoubleTriple calculateMetrics(List<Spot> list) {
        int n = list.size();
        if (n == 0) return DoubleTriple.of();

        double sum = 0;
        double count = 0;

        for (Spot spot : list) {
            sum += spot.getNumber();
            if (spot.isRed()) count++;
        }

        double mean = sum / n;
        double rate = count / n;

        double diff = 0;
        for (Spot spot : list) {
            diff += Math.pow(spot.getNumber() - mean, 2);
        }
        return DoubleTriple.of(rate, Math.sqrt(diff / n), mean);
    }
}
