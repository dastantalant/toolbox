package com.trading.roulette.strategy.strategies.hybrid;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.SpotPrediction;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Strategy(tags = {Tag.HYBRID})
public class DynamicHybridPredictorStrategy extends AbstractHybridPredictorStrategy {

    private final Queue<Boolean> markovSuccess;
    private final Queue<Boolean> countSuccess;

    public DynamicHybridPredictorStrategy(Config config) {
        super(config);
        markovSuccess = new LinkedList<>();
        countSuccess = new LinkedList<>();
    }

    @Override
    public void onRoundCustom(Spot spot, List<Bet> list) {
        updatePredictorPerformance(spot);
        adjustPredictorWeights();
    }

    private void updatePredictorPerformance(Spot spot) {
        isPredictorCorrect(predictorMarkov,  spot, markovSuccess);
        isPredictorCorrect(predictorCount,  spot, countSuccess);
    }

    private void isPredictorCorrect(Predictor predictor,  Spot spot, Queue<Boolean> queue) {
        Optional<Spot> optional = predictor.predictSpot(getState(), getConfig()).stream()
                .max(Comparator.comparingDouble(SpotPrediction::probability))
                .map(SpotPrediction::spot);

        queue.add(optional.isPresent() && optional.get().equals(spot));
        if (queue.size() > window) {
            queue.poll();
        }
    }

    private void adjustPredictorWeights() {
        double markovRate = calculate(markovSuccess);
        double countRate = calculate(countSuccess);

        double sum = markovRate + countRate;
        if (sum > 0) {
            dynamicMarkov = markovRate / sum;
            dynamicCount = countRate / sum;
        }
    }

    private double calculate(Queue<Boolean> successQueue) {
        if (successQueue.isEmpty()) return 0.0;
        return (double) Collections.frequency(successQueue, true) / successQueue.size();
    }

    @Override
    public List<Bet> decide() {
        List<SpotPrediction> top = getTop();
        if (top.isEmpty()) return List.of();
        return process( top, (long) (getVirtualBalance() * fraction));
    }
}