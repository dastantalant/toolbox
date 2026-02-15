package com.trading.roulette.strategy.strategies.hybrid;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.utils.BetHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractHybridPredictorStrategy extends AbstractStrategy {

    protected final Predictor predictorMarkov;
    protected final Predictor predictorCount;
    protected final double markov;
    protected final double count;
    protected final int limit;
    protected final double confidence;
    protected final double fraction;
    protected final int window;
    protected final double lowWinRate;
    protected final double highWinRate;
    protected final int consecutive;
    protected final double cautious;

    protected double dynamicConfidence;
    protected double dynamicFraction;
    protected double dynamicMarkov;
    protected double dynamicCount;

    protected AbstractHybridPredictorStrategy(Config config) {
        this(config, HybridContext.of());
    }

    protected AbstractHybridPredictorStrategy(Config config, HybridContext hContext) {
        super(config);
        predictorMarkov = hContext.getPredictorMarkov();
        predictorCount = hContext.getPredictorCount();
        markov = hContext.getMarkov();
        count = hContext.getCount();
        limit = hContext.getLimit();
        confidence = hContext.getConfidence();
        fraction = hContext.getFraction();
        window = hContext.getWindow();
        lowWinRate = hContext.getLowWinRate();
        highWinRate = hContext.getHighWinRate();
        consecutive = hContext.getConsecutive();
        cautious = hContext.getCautious();

        dynamicConfidence = confidence;
        dynamicFraction = fraction;
        dynamicMarkov = markov;
        dynamicCount = count;
    }

    public List<SpotPrediction> getTop() {
        return getTop( markov, count, confidence);
    }

    public List<SpotPrediction> getTop(double markov, double count, double confidence) {
        return getTop( markov, count, confidence, limit);
    }

    public List<SpotPrediction> getTop(double markov, double count, double confidence, int limit) {
        Map<Spot, Double> markovPred = predictorMarkov.predictSpot(getState(), getConfig()).stream()
                .collect(Collectors.toMap(SpotPrediction::spot, SpotPrediction::probability));
        Map<Spot, Double> countPred = predictorCount.predictSpot(getState(), getConfig()).stream()
                .collect(Collectors.toMap(SpotPrediction::spot, SpotPrediction::probability));

        List<Spot> list = Spot.availableList(getConfig().rouletteType());
        Map<Spot, Double> collected = list.stream()
                .collect(Collectors.toMap(spot -> spot,
                        spot -> (markovPred.getOrDefault(spot, 0.0) * markov)
                                + (countPred.getOrDefault(spot, 0.0) * count)));
        return collected.entrySet().stream()
                .map(entry -> SpotPrediction.of(entry.getKey(), entry.getValue()))
                .filter(item -> item.probability() >= confidence)
                .sorted(Comparator.comparingDouble(SpotPrediction::probability).reversed())
                .limit(limit)
                .toList();
    }

    public List<Bet> process(List<SpotPrediction> top, long budget) {
        if (budget < getMinBet()) return List.of();
        double sum = top.stream()
                .mapToDouble(SpotPrediction::probability)
                .sum();
        if (sum == 0) return List.of();
        List<Bet> result = new ArrayList<>(top.size());
        for (SpotPrediction item : top) {
            long value = (long) (budget * (item.probability() / sum));
            long clamp = Math.clamp(value, getMinBet(), getMaxBet());
            if (clamp <= getVirtualBalance()) {
                result.add(Bet.of(BetHelper.straightUp(item.spot()), clamp));
            }
        }
        return result;
    }
}
