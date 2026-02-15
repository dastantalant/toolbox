package com.trading.roulette.api.runner;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.Subscriber;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.exception.StrategyException;
import com.trading.roulette.api.model.Snapshot;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.runner.supplier.SpotSupplier;

public sealed interface Simulator permits Simulator.Base {

    RunnerResult run();

    static Simulator create(MetaConfig meta) {
        try {
            return create(meta.clazz().getConstructor(Config.class).newInstance(meta.config()), meta.supplier());
        } catch (Exception e) {
            throw new StrategyException("Create Simulator failed", e);
        }
    }

    static Simulator create(IStrategy strategy, SpotSupplier supplier) {
        Subscriber subscriber = Subscriber.storage();
        strategy.addSubscriber(subscriber);
        return new Base(Runner.of(strategy), subscriber, supplier);
    }

    record Base(Runner runner, Subscriber subscriber, SpotSupplier supplier) implements Simulator {
        public RunnerResult run() {
            boolean isLive = runner.isLive();
            boolean hasNext = supplier.hasNext();
            Snapshot snapshot = null;
            while (isLive && hasNext) {
                Spot spot = supplier.get();
                if (spot == null) break;
                snapshot = runner.victory(spot);
                isLive = runner.isLive();
                hasNext = supplier.hasNext();
            }
            return RunnerResult.of(runner.getStrategy().getName(), snapshot, subscriber.deque());
        }
    }
}
