package com.trading.roulette.strategy.strategies.manager;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.strategy.strategies.HotNumbersStrategy;
import com.trading.roulette.strategy.strategies.MeanReversionStrategy;
import com.trading.roulette.strategy.strategies.ParoliStrategy;
import com.trading.roulette.strategy.strategies.hybrid.HybridPredictorStrategy;
import com.trading.roulette.strategy.strategies.kelly.KellyCriterionStrategy;
import com.trading.roulette.strategy.strategies.volatility.AdaptiveVolatilityStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Strategy(tags = Tag.MANAGER)
public class AdvancedManager extends BaseManager {

    private IStrategy currentChampion;
    private long roundsUntilReevaluation;

    private final Map<IStrategy, LinkedList<Double>> returnHistories;

    private final Map<IStrategy, List<Bet>> pendingVirtualBets;

    public AdvancedManager(Config config) {
        super(config);
        List<IStrategy> portfolioList = Stream.of(
                        HybridPredictorStrategy.class,
                        MeanReversionStrategy.class,
                        KellyCriterionStrategy.class,
                        AdaptiveVolatilityStrategy.class,
                        HotNumbersStrategy.class,
                        ParoliStrategy.class)
                .map(clazz -> super.create(clazz, config))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        add(new ArrayList<>(portfolioList));

        currentChampion = portfolioList.isEmpty() ? null : portfolioList.getFirst();
        roundsUntilReevaluation = REEVALUATION_PERIOD;

        returnHistories = new HashMap<>();
        pendingVirtualBets = new HashMap<>();

        portfolioList.forEach(strategy -> returnHistories.put(strategy, new LinkedList<>()));
    }

    @Override
    public List<Bet> decide() {
        if (currentChampion == null) {
            return List.of();
        }
        roundsUntilReevaluation--;
        if (roundsUntilReevaluation <= 0) {
            reevaluateChampionBySharpeRatio();
            roundsUntilReevaluation = REEVALUATION_PERIOD;
        }

        pendingVirtualBets.clear();
        for (IStrategy strategy : portfolio) {
            if (strategy == currentChampion) {
                strategy.getState().setVirtualBalance(this.getVirtualBalance());
            }
            List<Bet> bets = strategy.decide();
            pendingVirtualBets.put(strategy, bets);
        }

        List<Bet> championBets = pendingVirtualBets.getOrDefault(currentChampion, List.of());
        setLastBets(championBets);
        return championBets;
    }

    @Override
    public void onRoundCustom(Spot spot, List<Bet> betList) {
        for (IStrategy strategy : portfolio) {
            long balanceBefore = strategy.getVirtualBalance();
            List<Bet> virtualBets = pendingVirtualBets.getOrDefault(strategy, List.of());
             strategy.commitResult( spot,virtualBets);
            long balanceAfter = strategy.getVirtualBalance();
            double dailyReturn = (balanceBefore == 0) ? 0.0 : (double) (balanceAfter - balanceBefore) / balanceBefore;
            LinkedList<Double> history = returnHistories.get(strategy);
            if (history != null) {
                history.add(dailyReturn);
                if (history.size() > REEVALUATION_PERIOD) {
                    history.removeFirst();
                }
            }
        }
    }

    private void reevaluateChampionBySharpeRatio() {
        Optional<IStrategy> newChampionOpt = portfolio.stream()
                .max(Comparator.comparingDouble(this::calculateSharpeRatio));

        newChampionOpt.ifPresent(strategy -> {
            if (strategy != currentChampion) {
                currentChampion = strategy;
            }
        });
    }

    private double calculateSharpeRatio(IStrategy strategy) {
        LinkedList<Double> returns = returnHistories.get(strategy);
        if (returns == null || returns.size() < 2) {
            return -Double.MAX_VALUE;
        }
        double meanReturn = returns.stream().mapToDouble(d -> d).average().orElse(0.0);
        double standardDeviation = Math.sqrt(
                returns.stream()
                        .mapToDouble(r -> Math.pow(r - meanReturn, 2))
                        .average().orElse(0.0));
        if (standardDeviation == 0) {
            return (meanReturn > 0) ? Double.MAX_VALUE : 0;
        }
        return meanReturn / standardDeviation;
    }
}