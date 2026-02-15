package com.trading.roulette.api.model;

import com.trading.roulette.api.enums.Constants;
import com.trading.roulette.api.enums.Spot;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

public class Market {
    private final Deque<Spot> spotHistory;
    private final Map<Spot, Integer> spotFrequencies;
    private long totalProcessed;

    public Market() {
        spotHistory = new ArrayDeque<>(Constants.SPOT_HISTORY_LIST_SIZE);
        spotFrequencies = new EnumMap<>(Spot.class);
        totalProcessed = 0;
    }

    public void addSpot(Spot spot) {
        if (spotHistory.size() >= Constants.SPOT_HISTORY_LIST_SIZE) {
            Spot removedSpot = spotHistory.pollFirst();
            spotFrequencies.compute(removedSpot, (_, count) -> count == null || count <= 1 ? null : count - 1);
        }
        spotHistory.addLast(spot);
        spotFrequencies.merge(spot, 1, Integer::sum);
        totalProcessed++;
    }

    public double getRate(Predicate<Spot> condition) {
        if (spotHistory.isEmpty()) return 0.0;
        double matchCount = spotFrequencies.entrySet().stream()
                .filter(entry -> condition.test(entry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
        return matchCount / spotHistory.size();
    }

    public Deque<Spot> getHistory() {
        return spotHistory;
    }

    public Map<Spot, Integer> getFrequencies() {
        return Collections.unmodifiableMap(spotFrequencies);
    }

    public Spot getLastSpot() {
        return spotHistory.isEmpty() ? null : spotHistory.getLast();
    }

    public long getCurrentLoopCount() {
        return totalProcessed;
    }

    public double getRedRate() {
        return getRate(Spot::isRed);
    }

    public double getBlackRate() {
        return getRate(Spot::isBlack);
    }
}