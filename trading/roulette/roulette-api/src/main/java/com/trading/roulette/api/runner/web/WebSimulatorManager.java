package com.trading.roulette.api.runner.web;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.Subscriber;
import com.trading.roulette.api.exception.StrategyException;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.runner.Runner;
import com.trading.roulette.api.runner.SharedSimulator;
import com.trading.roulette.api.runner.supplier.SpotSupplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface WebSimulatorManager {

    Deque<WebSimulateResult> run();

    static WebSimulatorManager create(Collection<Class<? extends IStrategy>> strategies, Config config, SpotSupplier supplier) {
        return new WebOptimized(strategies, config, supplier);
    }

    record WebOptimized(Collection<Class<? extends IStrategy>> strategies, Config config, SpotSupplier supplier)
            implements WebSimulatorManager {

        @Override
        public Deque<WebSimulateResult> run() {
            List<Runner> runners = new ArrayList<>();
            Map<Runner, SamplingSubscriber> subscribersMap = new IdentityHashMap<>();
            Map<Runner, Subscriber> sharedSubscribersMap = new IdentityHashMap<>();
            Map<Runner, String> strategyNames = new IdentityHashMap<>();
            long maxRounds = supplier.getMax();
            for (Class<? extends IStrategy> clazz : strategies) {
                IStrategy strategy;
                try {
                    strategy = clazz.getConstructor(Config.class).newInstance(config);
                } catch (Exception e) {
                    throw new StrategyException("Strategy " + clazz.getSimpleName() + " not found", e);
                }
                SamplingSubscriber sub = new SamplingSubscriber(maxRounds);
                strategy.addSubscriber(sub);
                Runner runner = Runner.of(strategy);
                runners.add(runner);
                subscribersMap.put(runner, sub);
                sharedSubscribersMap.put(runner, sub);
                strategyNames.put(runner, strategy.getName());
            }
            if (runners.isEmpty()) return new LinkedList<>();
            SharedSimulator simulator = new SharedSimulator(runners, sharedSubscribersMap, supplier);
            simulator.run();
            Deque<WebSimulateResult> results = new LinkedList<>();
            for (Runner runner : runners) {
                SamplingSubscriber sub = subscribersMap.get(runner);
                String sName = strategyNames.get(runner);
                results.add(WebSimulateResult.of(sName, config.name(), sub.getFinalStats(), sub.getChart()));
            }

            return results;
        }
    }
}