package com.trading.roulette.strategy.strategies.hybrid;

import com.trading.roulette.strategy.predictor.EfficientCountPredictor;
import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;
import com.trading.roulette.api.Predictor;

public class HybridContext {
    private final Predictor predictorMarkov;
    private final Predictor predictorCount;
    private final double markov;
    private final double count;
    private final int limit;
    private final double confidence;
    private final double fraction;
    private final int window;
    private final double lowWinRate;
    private final double highWinRate;
    private final int consecutive;
    private final double cautious;

    public HybridContext(Predictor predictorMarkov, Predictor predictorCount, double markov, double count, int limit, double confidence, double fraction, int window, double lowWinRate, double highWinRate, int consecutive, double cautious) {
        this.predictorMarkov = predictorMarkov;
        this.predictorCount = predictorCount;
        this.markov = markov;
        this.count = count;
        this.limit = limit;
        this.confidence = confidence;
        this.fraction = fraction;
        this.window = window;
        this.lowWinRate = lowWinRate;
        this.highWinRate = highWinRate;
        this.consecutive = consecutive;
        this.cautious = cautious;
    }

    public static HybridContext of() {
        return HybridContext.builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Predictor getPredictorMarkov() {
        return predictorMarkov;
    }

    public Predictor getPredictorCount() {
        return predictorCount;
    }

    public double getMarkov() {
        return markov;
    }

    public double getCount() {
        return count;
    }

    public int getLimit() {
        return limit;
    }

    public double getConfidence() {
        return confidence;
    }

    public double getFraction() {
        return fraction;
    }

    public int getWindow() {
        return window;
    }

    public double getLowWinRate() {
        return lowWinRate;
    }

    public double getHighWinRate() {
        return highWinRate;
    }

    public int getConsecutive() {
        return consecutive;
    }

    public double getCautious() {
        return cautious;
    }

    public static class Builder {
        private Predictor predictorMarkov = new MultiOrderMarkovPredictor();
        private Predictor predictorCount = new EfficientCountPredictor();
        private double markov = 0.3;
        private double count = 0.2;
        private int limit = 4;
        private double confidence = 0.08;
        private double fraction = 0.05;
        private int window = 50;
        private double lowWinRate = 0.45;
        private double highWinRate = 0.60;
        private int consecutive = 3;
        private double cautious = 0.025;

        public Builder predictorMarkov(Predictor predictorMarkov) {
            this.predictorMarkov = predictorMarkov;
            return this;
        }

        public Builder predictorCount(Predictor predictorCount) {
            this.predictorCount = predictorCount;
            return this;
        }

        public Builder markov(double markov) {
            this.markov = markov;
            return this;
        }

        public Builder count(double count) {
            this.count = count;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder fraction(double fraction) {
            this.fraction = fraction;
            return this;
        }

        public Builder window(int window) {
            this.window = window;
            return this;
        }

        public Builder lowWinRate(double lowWinRate) {
            this.lowWinRate = lowWinRate;
            return this;
        }

        public Builder highWinRate(double highWinRate) {
            this.highWinRate = highWinRate;
            return this;
        }

        public Builder consecutive(int consecutive) {
            this.consecutive = consecutive;
            return this;
        }

        public Builder cautious(double cautious) {
            this.cautious = cautious;
            return this;
        }

        public HybridContext build() {
            return new HybridContext(predictorMarkov, predictorCount, markov, count, limit, confidence, fraction, window, lowWinRate, highWinRate, consecutive, cautious);
        }
    }

}
