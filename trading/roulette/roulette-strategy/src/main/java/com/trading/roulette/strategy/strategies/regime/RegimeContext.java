package com.trading.roulette.strategy.strategies.regime;


public class RegimeContext {
    private final int historyWindow;
    private final double trendThreshold;
    private final double volatilityLowThreshold;
    private final int trendStreak;
    private final int alternatingStreak;

    public RegimeContext(int historyWindow, double trendThreshold, double volatilityLowThreshold, int trendStreak, int alternatingStreak) {
        this.historyWindow = historyWindow;
        this.trendThreshold = trendThreshold;
        this.volatilityLowThreshold = volatilityLowThreshold;
        this.trendStreak = trendStreak;
        this.alternatingStreak = alternatingStreak;
    }

    public static RegimeContext of() {
        return RegimeContext.builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getHistoryWindow() {
        return historyWindow;
    }

    public double getTrendThreshold() {
        return trendThreshold;
    }

    public double getVolatilityLowThreshold() {
        return volatilityLowThreshold;
    }

    public int getTrendStreak() {
        return trendStreak;
    }

    public int getAlternatingStreak() {
        return alternatingStreak;
    }

    public static class Builder {
        private int historyWindow = 15;
        private double trendThreshold = 0.70;
        private double volatilityLowThreshold = 6.0;
        private int trendStreak = 3;
        private int alternatingStreak = 4;

        public Builder historyWindow(int historyWindow) {
            this.historyWindow = historyWindow;
            return this;
        }

        public Builder trendThreshold(double trendThreshold) {
            this.trendThreshold = trendThreshold;
            return this;
        }

        public Builder volatilityLowThreshold(double volatilityLowThreshold) {
            this.volatilityLowThreshold = volatilityLowThreshold;
            return this;
        }

        public Builder trendStreak(int trendStreak) {
            this.trendStreak = trendStreak;
            return this;
        }

        public Builder alternatingStreak(int alternatingStreak) {
            this.alternatingStreak = alternatingStreak;
            return this;
        }

        public RegimeContext build() {
            return new RegimeContext(historyWindow, trendThreshold, volatilityLowThreshold, trendStreak, alternatingStreak);
        }
    }

}