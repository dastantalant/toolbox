package com.trading.roulette.api.runner;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.Subscriber;
import com.trading.roulette.api.exception.StrategyException;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.runner.supplier.SpotSupplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public sealed interface SimulatorManager permits SimulatorManager.Base, SimulatorManager.Shared {

    Deque<SimulateResult> run();

    static SimulatorManager create(Collection<MetaConfig> meta) {
        return new Base(meta);
    }

    static SimulatorManager create(Collection<MetaConfigMap> meta, SpotSupplier sharedSupplier) {
        return new Shared(meta, sharedSupplier);
    }

    record Base(Collection<MetaConfig> meta) implements SimulatorManager {
        @Override
        public Deque<SimulateResult> run() {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                Collection<Callable<SimulateResult>> tasks = meta.stream()
                        .map(item -> (Callable<SimulateResult>) () -> {
                            RunnerResult run = Simulator.create(item).run();
                            return SimulateResult.of(item.config().name(), run.strategy(), run);
                        })
                        .toList();
                return executor.invokeAll(tasks).stream()
                        .map(this::getFutureResult)
                        .collect(Collectors.toCollection(LinkedList::new));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new StrategyException("Simulation interrupted", e);
            }
        }

        private SimulateResult getFutureResult(Future<SimulateResult> future) {
            try {
                return future.get();
            } catch (Exception e) {
                throw new StrategyException("Simulation failed", e);
            }
        }
    }

    record Shared(Collection<MetaConfigMap> meta, SpotSupplier sharedSupplier) implements SimulatorManager {
        @Override
        public Deque<SimulateResult> run() {
            Collection<Runner> runners = new ArrayList<>();
            Map<Runner, Subscriber> subscribers = new HashMap<>();
            Map<Runner, String> nameMap = new IdentityHashMap<>();
            for (MetaConfigMap item : meta) {
                IStrategy strategy;
                try {
                    strategy = item.clazz().getConstructor(Config.class).newInstance(item.config());
                } catch (Exception e) {
                    throw new StrategyException("Cannot instantiate " + item.clazz().getName(), e);
                }
                Subscriber sub = Subscriber.storage();
                strategy.addSubscriber(sub);
                Runner runner = Runner.of(strategy);
                runners.add(runner);
                subscribers.put(runner, sub);
                nameMap.put(runner, item.config().name());
            }
            if (runners.isEmpty()) {
                return new LinkedList<>();
            }
            SharedSimulator simulator = new SharedSimulator(runners, subscribers, sharedSupplier);
            Map<Runner, RunnerResult> executionResults = simulator.run();
            return executionResults.entrySet().stream()
                    .map(entry -> SimulateResult.of(nameMap.getOrDefault(entry.getKey(), "UNKNOWN"), entry.getValue().strategy(), entry.getValue()))
                    .collect(Collectors.toCollection(LinkedList::new));
        }
    }
}