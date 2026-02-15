package com.trading.roulette.api.runner;

import com.trading.roulette.api.Subscriber;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Snapshot;
import com.trading.roulette.api.runner.supplier.SpotSupplier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public record SharedSimulator(Collection<Runner> runners, Map<Runner, Subscriber> subscribers, SpotSupplier supplier) {

    public Map<Runner, RunnerResult> run() {
        while (supplier.hasNext()) {
            boolean anyLive = runners.stream().anyMatch(Runner::isLive);
            if (!anyLive) {
                break;
            }
            Spot spot = supplier.get();
            if (spot == null) break;
            for (Runner runner : runners) {
                if (runner.isLive()) {
                    runner.victory(spot);
                }
            }
        }
        Map<Runner, RunnerResult> results = new LinkedHashMap<>();
        for (Runner runner : runners) {
            Snapshot stop = runner.stop();
            Subscriber sub = subscribers.get(runner);
            results.put(runner, RunnerResult.of(runner.getStrategy().getName(), stop, sub != null ? sub.deque() : new LinkedList<>()));
        }
        return results;
    }
}