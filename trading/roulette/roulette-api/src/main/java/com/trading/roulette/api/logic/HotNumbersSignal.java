package com.trading.roulette.api.logic;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.State;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HotNumbersSignal implements SignalGenerator {
    private final int count;
    private final boolean reverse;

    public HotNumbersSignal(int count) {
        this(count, false);
    }

    public HotNumbersSignal(int count, boolean reverse) {
        this.count = count;
        this.reverse = reverse;
    }

    @Override
    public List<BetType> generate(State state) {
        Map<Spot, Integer> frequencies = state.getMarket().getFrequencies();
        if (frequencies.isEmpty()) return List.of();
        Comparator<Map.Entry<Spot, Integer>> comparator = Map.Entry.comparingByValue();
        if (reverse) {
        } else {
            comparator = comparator.reversed();
        }
        return frequencies.entrySet().stream()
                .sorted(comparator)
                .limit(count)
                .map(entry -> BetType.straightUpBetFor(entry.getKey()))
                .toList();
    }
}