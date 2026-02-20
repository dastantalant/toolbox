package com.trading.roulette.strategy.strategies.manager;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.model.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseManager extends AbstractStrategy implements Manager {
    protected static final int REEVALUATION_PERIOD = 25;

    protected final List<IStrategy> portfolio;

    protected BaseManager(Config config) {
        super(config);
        portfolio = new ArrayList<>();
    }

    @Override
    public List<IStrategy> getPortfolio() {
        return List.copyOf(portfolio);
    }

    public IStrategy add(IStrategy strategy) {
        portfolio.add(strategy);
        return strategy;
    }

    public List<IStrategy> add(List<IStrategy> strategies) {
        strategies.forEach(this::add);
        return strategies;
    }

    public IStrategy remove(IStrategy strategy) {
        portfolio.remove(strategy);
        return strategy;
    }

    public Optional<IStrategy> create(Class<? extends IStrategy> clazz, Config config) {
        try {
            return Optional.of(clazz.getConstructor(Config.class).newInstance(config));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
