package com.trading.roulette.strategy.strategies.manager;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.strategy.strategies.MeanReversionStrategy;
import com.trading.roulette.strategy.strategies.hybrid.HybridPredictorStrategy;
import com.trading.roulette.strategy.strategies.kelly.KellyCriterionStrategy;
import com.trading.roulette.strategy.strategies.volatility.AdaptiveVolatilityStrategy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Strategy(tags = Tag.MANAGER)
public class StrategyManager extends BaseManager {

    private IStrategy currentChampion;
    private long roundsUntilReevaluation;

    private final Map<IStrategy, List<Bet>> pendingVirtualBets = new HashMap<>();

    public StrategyManager(Config config) {
        super(config);
        List<IStrategy> portfolio = List.of(new HybridPredictorStrategy(config),
                new MeanReversionStrategy(config),
                new KellyCriterionStrategy(config),
                new AdaptiveVolatilityStrategy(config));

        currentChampion = portfolio.getFirst();
        roundsUntilReevaluation = REEVALUATION_PERIOD;
        add(portfolio);
    }

    @Override
    public List<Bet> decide() {
        if (currentChampion == null) return List.of();
        roundsUntilReevaluation--;
        if (roundsUntilReevaluation <= 0) {
            reevaluateChampion();
            roundsUntilReevaluation = REEVALUATION_PERIOD;
        }

        pendingVirtualBets.clear();
        for (IStrategy strategy : portfolio) {
            if (strategy instanceof AbstractStrategy bs) {
                bs.setVirtualBalance(this.getVirtualBalance());
            }
            pendingVirtualBets.put(strategy, strategy.decide());
        }

        List<Bet> championBets = pendingVirtualBets.getOrDefault(currentChampion, List.of());
        setLastBets(championBets);
        return championBets;
    }

    @Override
    public void onRoundCustom(Spot spot, List<Bet> realBets) {
        for (IStrategy strategy : portfolio) {
            List<Bet> virtualBets = pendingVirtualBets.getOrDefault(strategy, List.of());
            strategy.commitResult(spot, virtualBets);
        }
    }

    private void reevaluateChampion() {
        portfolio.stream()
                .max(Comparator.comparingLong(IStrategy::getVirtualBalance))
                .ifPresent(strategy -> {
                    if (strategy != currentChampion && strategy instanceof AbstractStrategy) {
                        currentChampion = strategy;
                    }
                });
    }
}