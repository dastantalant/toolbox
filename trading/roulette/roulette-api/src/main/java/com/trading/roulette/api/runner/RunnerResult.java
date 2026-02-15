package com.trading.roulette.api.runner;


import com.trading.roulette.api.model.Snapshot;

import java.util.Deque;

public record RunnerResult(String strategy, Snapshot snapshot, Deque<Snapshot> deque) {
    public static RunnerResult of(String strategy, Snapshot snapshot, Deque<Snapshot> deque) {
        return new RunnerResult(strategy, snapshot, deque);
    }
}