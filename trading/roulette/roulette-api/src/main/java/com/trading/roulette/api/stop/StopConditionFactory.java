package com.trading.roulette.api.stop;

import com.trading.roulette.api.model.config.StopConditionConfig;

import java.util.ArrayList;
import java.util.List;

public final class StopConditionFactory {

    private StopConditionFactory() {
    }

    public static List<StopCondition> from(StopConditionConfig config, Long startBalance) {
        List<StopCondition> conditions = new ArrayList<>();

        // 1. Zero Balance (Hard Limit)
        conditions.add(strategy -> strategy.getVirtualBalance() <= 0);

        // 2. Max Rounds
        if (config.maxRoundsStop() != null && config.maxRoundsStop() > 0) {
            long limit = config.maxRoundsStop();
            conditions.add(strategy -> strategy.getBetCount() >= limit);
        }

        // 3. Initial Stop Loss (Fixed amount from start)
        if (config.stopLossPercentageStop() > 0) {
            long limit = (long) (startBalance * (1.0 - config.stopLossPercentageStop() / 100.0));
            conditions.add(strategy -> strategy.getVirtualBalance() <= limit);
        }

        // 4. Take Profit
        if (config.takeProfitPercentageStop() > 0) {
            long limit = (long) (startBalance * (1.0 + config.takeProfitPercentageStop() / 100.0));
            conditions.add(strategy -> strategy.getVirtualBalance() >= limit);
        }

        // 5. Trailing Stop (Based on Peak Balance)
        if (config.trailingStopPercentageStop() > 0) {
            double percent = config.trailingStopPercentageStop() / 100.0;
            conditions.add(strategy -> {
                if (strategy.getPeakBalance() <= startBalance) return false; // Only active in profit? Optional logic.
                long limit = (long) (strategy.getPeakBalance() * (1.0 - percent));
                return strategy.getVirtualBalance() <= limit;
            });
        }

        // 6. Max Drawdown (Global stats)
        if (config.maxDrawdownPercentageStop() > 0) {
            double limit = config.maxDrawdownPercentageStop() / 100.0;
            conditions.add(strategy -> strategy.getMaxDrawdown() >= limit);
        }

        // 7. Low Win Rate (Gated by min rounds)
        if (config.minWinRateThresholdStop() > 0) {
            double minRate = config.minWinRateThresholdStop() / 100.0;
            long minRounds = config.minRoundsForWinRateStop();
            conditions.add(strategy -> {
                if (strategy.getBetCount() < minRounds) return false;
                double currentRate = (double) strategy.getWonCount() / strategy.getBetCount();
                return currentRate < minRate;
            });
        }

        // --- Stateful Conditions (Keep as classes) ---

        // 8. Stagnation
        if (config.stagnationWindowStop() > 0) {
            conditions.add(new StagnationCondition(config.stagnationWindowStop()));
        }

        // 9. Streaks
        if (config.maxWinStreakStop() > 0 || config.maxLossStreakStop() > 0) {
            conditions.add(new StreakStopCondition(
                    config.maxLossStreakStop(),
                    config.maxWinStreakStop(),
                    startBalance
            ));
        }

        // 10. Equity Curve
        if (config.equityCurveSmaPeriodStop() != null && config.equityCurveSmaPeriodStop() > 0) {
            conditions.add(new EquityCurveCondition(config.equityCurveSmaPeriodStop()));
        }

        return conditions;
    }
}