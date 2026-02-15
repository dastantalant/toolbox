package com.trading.roulette.strategy.strategies.kelly;

import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;
import com.trading.roulette.api.Predictor;

public class KellyContext {
    private final double kelly;
    private final double maxBetPercentageCap;
    private final double minProbabilityThreshold;
    private final Predictor predictor;

    public KellyContext(double kelly, double maxBetPercentageCap, double minProbabilityThreshold, Predictor predictor) {
        this.kelly = kelly;
        this.maxBetPercentageCap = maxBetPercentageCap;
        this.minProbabilityThreshold = minProbabilityThreshold;
        this.predictor = predictor;
    }

    public static KellyContext of() {
        return KellyContext.builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public double getKelly() {
        return kelly;
    }

    public double getMaxBetPercentageCap() {
        return maxBetPercentageCap;
    }

    public double getMinProbabilityThreshold() {
        return minProbabilityThreshold;
    }

    public Predictor getPredictor() {
        return predictor;
    }

    public static class Builder {
        private double kelly = 0.3;
        private double maxBetPercentageCap = 0.05;
        private double minProbabilityThreshold = 0.028;
        private Predictor predictor = new MultiOrderMarkovPredictor();

        public KellyContext build() {
            return new KellyContext(kelly, maxBetPercentageCap, minProbabilityThreshold, predictor);
        }

        public Builder kelly(double kelly) {
            this.kelly = kelly;
            return this;
        }

        public Builder maxBetPercentageCap(double maxBetPercentageCap) {
            this.maxBetPercentageCap = maxBetPercentageCap;
            return this;
        }

        public Builder minProbabilityThreshold(double minProbabilityThreshold) {
            this.minProbabilityThreshold = minProbabilityThreshold;
            return this;
        }

        public Builder predictor(Predictor predictor) {
            this.predictor = predictor;
            return this;
        }
    }
}
