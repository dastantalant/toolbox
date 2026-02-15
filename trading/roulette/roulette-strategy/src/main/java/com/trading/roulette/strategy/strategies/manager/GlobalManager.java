package com.trading.roulette.strategy.strategies.manager;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;
import com.trading.roulette.api.sizing.MartingaleSizing;
import com.trading.roulette.strategy.strategies.EnsemblePredictionStrategy;
import com.trading.roulette.strategy.strategies.balance.ColorBalanceStrategy;
import com.trading.roulette.strategy.strategies.balance.EvenOddBalanceStrategy;
import com.trading.roulette.strategy.strategies.martingale.GrandMartingaleStrategy;
import com.trading.roulette.strategy.strategies.martingale.MartingaleStrategy;
import com.trading.roulette.strategy.strategies.martingale.MartingaleStrategy4;
import com.trading.roulette.api.utils.BetHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Strategy(tags = Tag.MANAGER)
public class GlobalManager extends BaseManager {

    private static final int ACTIVE_STRATEGIES_COUNT = 3;
    private static final int PERFORMANCE_WINDOW = 50;

    private final List<ManagedStrategy> pool;
    private List<IStrategy> activeStrategies;

    public GlobalManager(Config config) {
        super(config);
        pool = new ArrayList<>();
        activeStrategies = new ArrayList<>();
        List<IStrategy> portfolioList = Stream.of(GrandMartingaleStrategy.class,
                        MartingaleStrategy4.class,
                        EvenOddBalanceStrategy.class,
                        ColorBalanceStrategy.class,
                        MartingaleStrategy.class)
                .map(item -> create(item, config))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(s -> (IStrategy) ModularStrategy.of(config, new MultiOrderMarkovPredictor(), MartingaleSizing.of()))
                .map(s -> (IStrategy) new EnsemblePredictionStrategy(config))
                .peek(item -> pool.add(ManagedStrategy.of(item)))
                .toList();

        add(portfolioList);
    }

    @Override
    public void onRoundCustom(Spot spot, List<Bet> realBets) {
        for (ManagedStrategy managed : pool) {
            List<Bet> virtualBets = managed.lastVirtualBets;

            long profit = 0;
            if (!virtualBets.isEmpty()) {
                long payout = BetHelper.totalPayout(virtualBets, spot);
                long cost = BetHelper.totalBetValue(virtualBets);
                profit = payout - cost;
            }

            managed.updateScore(profit, PERFORMANCE_WINDOW);

            managed.strategy.commitResult(spot, virtualBets);
        }

        rebalancePortfolio();
    }

    private void rebalancePortfolio() {
        activeStrategies = pool.stream()
                .sorted(Comparator.comparingLong(ManagedStrategy::getScore).reversed())
                .limit(ACTIVE_STRATEGIES_COUNT)
                .filter(ms -> ms.getScore() > 0)
                .map(ms -> ms.strategy)
                .toList();
    }

    @Override
    public List<Bet> decide() {
        for (ManagedStrategy managed : pool) {
            ((AbstractStrategy) managed.strategy).setVirtualBalance(getVirtualBalance());
            managed.lastVirtualBets = managed.strategy.decide();
        }

        if (activeStrategies.isEmpty()) {
            return List.of();
        }

        List<Bet> combinedBets = new ArrayList<>();
        for (IStrategy strategy : activeStrategies) {
            ManagedStrategy ms = pool.stream()
                    .filter(p -> p.strategy == strategy)
                    .findFirst()
                    .orElse(null);

            if (ms != null && ms.lastVirtualBets != null) {
                combinedBets.addAll(ms.lastVirtualBets);
            }
        }

        return mergeBets(combinedBets);
    }

    private List<Bet> mergeBets(List<Bet> rawBets) {
        if (rawBets.isEmpty()) return List.of();

        Map<BetType, Long> merged = new EnumMap<>(BetType.class);

        for (Bet bet : rawBets) {
            merged.merge(bet.type(), bet.value(), Long::sum);
        }

        nettingOpposites(merged, BetType.RED, BetType.BLACK);
        nettingOpposites(merged, BetType.EVEN, BetType.ODD);
        nettingOpposites(merged, BetType.ONE_TO_EIGHTEEN, BetType.NINETEEN_TO_THIRTY_SIX);

        return merged.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(entry -> {
                    long value = Math.min(entry.getValue(), getMaxBet());
                    return new Bet(entry.getKey(), Math.max(value, getMinBet()));
                })
                .toList();
    }

    private void nettingOpposites(Map<BetType, Long> map, BetType a, BetType b) {
        long valA = map.getOrDefault(a, 0L);
        long valB = map.getOrDefault(b, 0L);

        if (valA > 0 && valB > 0) {
            if (valA > valB) {
                map.put(a, valA - valB);
                map.remove(b);
            } else if (valB > valA) {
                map.put(b, valB - valA);
                map.remove(a);
            } else {
                map.remove(a);
                map.remove(b);
            }
        }
    }

    private static class ManagedStrategy {
        final IStrategy strategy;
        List<Bet> lastVirtualBets = List.of();
        final LinkedList<Long> profitHistory = new LinkedList<>();
        long currentScore = 0;

        public static ManagedStrategy of(IStrategy strategy) {
            return new ManagedStrategy(strategy);
        }

        ManagedStrategy(IStrategy strategy) {
            this.strategy = strategy;
        }

        void updateScore(long profit, int windowSize) {
            profitHistory.add(profit);
            currentScore += profit;
            if (profitHistory.size() > windowSize) {
                currentScore -= profitHistory.removeFirst();
            }
        }

        long getScore() {
            return currentScore;
        }
    }
}