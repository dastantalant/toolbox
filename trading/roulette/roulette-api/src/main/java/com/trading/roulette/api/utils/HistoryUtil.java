package com.trading.roulette.api.utils;

import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class HistoryUtil {
    private HistoryUtil() {}

    public static List<Spot> window(Deque<Spot> fullHistory, int windowSize) {
        if (fullHistory.isEmpty()) return List.of();
        int size = fullHistory.size();
        int window = Math.min(windowSize, size);
        List<Spot> result = new ArrayList<>(window);
        var iterator = fullHistory.descendingIterator();
        int collected = 0;
        while (iterator.hasNext() && collected < window) {
            result.add(iterator.next());
            collected++;
        }
        Collections.reverse(result);
        return result;
    }

    public static List<Spot> window(State state, int windowSize) {
        return window(state.getMarket().getHistory(), windowSize);
    }
}