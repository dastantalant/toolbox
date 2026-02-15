package com.trading.roulette.api;

import com.trading.roulette.api.enums.GenerateType;
import com.trading.roulette.api.exception.StrategyException;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.config.RawConfig;
import com.trading.roulette.api.runner.MetaConfigMap;
import com.trading.roulette.api.runner.Simulator;
import com.trading.roulette.api.runner.SimulatorManager;
import com.trading.roulette.api.runner.supplier.QueueSpotSupplier;
import com.trading.roulette.api.runner.supplier.RandomSpotSupplier;
import com.trading.roulette.api.runner.supplier.SpotSupplier;
import com.trading.roulette.api.runner.web.WebSimulatorManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;

@Deprecated
public final class StrategyFactory {

    private StrategyFactory() {
    }

    public static Simulator simulator(Class<? extends IStrategy> strategy, RawConfig rawConfig, SpotSupplier supplier) {
        try {
            var config = ConfigBuilder.from(rawConfig);
            var iStrategy = strategy.getConstructor(Config.class).newInstance(config);
            return Simulator.create(iStrategy, supplier);
        } catch (Exception e) {
            throw new StrategyException("Can't instantiate " + strategy.getCanonicalName(), e);
        }
    }

    public static SimulatorManager simulatorManager(Collection<Class<? extends IStrategy>> strategies, RawConfig rawConfig, Deque<Integer> deque) {
        return simulatorManager(strategies, rawConfig, QueueSpotSupplier.of(rawConfig.type(), deque));
    }

    public static SimulatorManager simulatorManager(Collection<Class<? extends IStrategy>> strategies, RawConfig rawConfig, long max) {
        return simulatorManager(strategies, rawConfig, GenerateType.RANDOM, max);
    }

    public static SimulatorManager simulatorManager(Collection<Class<? extends IStrategy>> strategies, RawConfig rawConfig, GenerateType generateType, long max) {
        return simulatorManager(strategies, rawConfig, RandomSpotSupplier.of(rawConfig.type(), generateType, max));
    }

    public static SimulatorManager simulatorManager(Collection<Class<? extends IStrategy>> strategies, RawConfig rawConfig, SpotSupplier supplier) {
        var config = ConfigBuilder.from(rawConfig);
        var list = new ArrayList<MetaConfigMap>(strategies.size());
        for (Class<? extends IStrategy> strategy : strategies) {
            list.add(new MetaConfigMap(config, strategy));
        }
        return SimulatorManager.create(list, supplier);
    }

    public static WebSimulatorManager webSimulatorManager(Collection<Class<? extends IStrategy>> strategies, RawConfig rawConfig, Deque<Integer> deque) {
        return webSimulatorManager(strategies, rawConfig, QueueSpotSupplier.of(rawConfig.type(), deque));
    }

    public static WebSimulatorManager webSimulatorManager(Collection<Class<? extends IStrategy>> strategies, RawConfig rawConfig, long max) {
        return webSimulatorManager(strategies, rawConfig, GenerateType.RANDOM, max);
    }

    public static WebSimulatorManager webSimulatorManager(Collection<Class<? extends IStrategy>> strategies, RawConfig rawConfig, GenerateType generateType, long max) {
        return webSimulatorManager(strategies, rawConfig, RandomSpotSupplier.of(rawConfig.type(), generateType, max));
    }

    public static WebSimulatorManager webSimulatorManager(Collection<Class<? extends IStrategy>> strategies, RawConfig rawConfig, SpotSupplier supplier) {
        var config = ConfigBuilder.from(rawConfig);
        return WebSimulatorManager.create(strategies, config, supplier);
    }
}
