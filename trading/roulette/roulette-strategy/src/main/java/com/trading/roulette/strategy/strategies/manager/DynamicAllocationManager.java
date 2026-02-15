package com.trading.roulette.strategy.strategies.manager;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.strategy.predictor.EfficientCountPredictor;
import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;
import com.trading.roulette.strategy.predictor.PatternPredictor;
import com.trading.roulette.api.sizing.DAlembertSizing;
import com.trading.roulette.api.sizing.KellySizing;
import com.trading.roulette.api.sizing.MartingaleSizing;
import com.trading.roulette.strategy.strategies.DAlembertStrategy;
import com.trading.roulette.strategy.strategies.balance.ColorBalanceStrategy;
import com.trading.roulette.strategy.strategies.balance.EvenOddBalanceStrategy;
import com.trading.roulette.strategy.strategies.martingale.GrandMartingaleStrategy;
import com.trading.roulette.strategy.strategies.martingale.MartingaleStrategy;
import com.trading.roulette.strategy.strategies.martingale.MartingaleStrategy4;
import com.trading.roulette.api.utils.BetHelper;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Strategy(tags = Tag.MANAGER)
public class DynamicAllocationManager extends BaseManager {

    private static final double GROWTH_FACTOR = 1.15;
    private static final double DECAY_FACTOR = 0.95; // 0.85
    private static final double MIN_WEIGHT_THRESHOLD = 0.01; // 0.05
    private static final double MAX_WEIGHT_CAP = 10.0;
    private static final double RESURRECTION_THRESHOLD = 0.1; // 0.5

    private final List<StrategyTracker> trackers;

    public DynamicAllocationManager(Config config) {
        super(config);
        trackers = new ArrayList<>();
        List<IStrategy> portfolio =
//                Stream.of(new CocomoStrategy(config),
//                        new MansuriansStrategy(config),
//                        new ParoliStrategy(config))
                Stream.of(GrandMartingaleStrategy.class,
                        MartingaleStrategy4.class,
                        EvenOddBalanceStrategy.class,
                        ColorBalanceStrategy.class,
                        MartingaleStrategy.class,
                        DAlembertStrategy.class)
                .map(item -> create(item, config))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(s -> (IStrategy) ModularStrategy.of(config, new MultiOrderMarkovPredictor(), MartingaleSizing.of()))
                .map(s -> (IStrategy) ModularStrategy.of(config, new EfficientCountPredictor(), DAlembertSizing.of()))
                .map(s -> (IStrategy) ModularStrategy.of(config, PatternPredictor.of(), KellySizing.of()))
                .toList();
        portfolio.forEach(item -> trackers.add(StrategyTracker.of(item)));
        add(portfolio);
    }

    @Override
    public void onRoundCustom(Spot spot, List<Bet> betList) {
        for (StrategyTracker tracker : trackers) {
            List<Bet> virtualBets = tracker.lastVirtualBets;
            long profit = 0;
            if (!virtualBets.isEmpty()) {
                long payout = BetHelper.totalPayout(virtualBets, spot);
                long cost = BetHelper.totalBetValue(virtualBets);
                profit = payout - cost;
            }

            tracker.strategy.commitResult( spot, virtualBets);

            if (!virtualBets.isEmpty()) {
                if (profit > 0) {
                    tracker.weight *= GROWTH_FACTOR;
                    tracker.consecutiveLosses = 0;
                } else {
                    tracker.weight *= DECAY_FACTOR;
                    tracker.consecutiveLosses++;
                }
            }

            if (tracker.weight < MIN_WEIGHT_THRESHOLD) {
                tracker.isActive = false;
            }

            if (!tracker.isActive && profit > 0 && tracker.consecutiveLosses == 0) {
                tracker.weight *= 1.1;
                if (tracker.weight > RESURRECTION_THRESHOLD) {
                    tracker.isActive = true;
                }
            }
            tracker.weight = Math.min(tracker.weight, MAX_WEIGHT_CAP);
        }
    }

    @Override
    public List<Bet> decide() {
        List<Bet> combinedBets = new ArrayList<>();

        for (StrategyTracker tracker : trackers) {
            ((AbstractStrategy) tracker.strategy).setVirtualBalance(getVirtualBalance());

            List<Bet> proposedBets = tracker.strategy.decide();
            tracker.lastVirtualBets = proposedBets;

            if (tracker.isActive && !proposedBets.isEmpty()) {
                for (Bet bet : proposedBets) {
                    long scaledValue = (long) (bet.value() * tracker.weight);
                    if (tracker.consecutiveLosses > 3) {
                        scaledValue = scaledValue / 2;
                    }
                    if (scaledValue >= 1) {
                        combinedBets.add(new Bet(bet.type(), scaledValue));
                    }
                }
            }
        }

        return mergeAndCheckLimits(combinedBets);
    }

    private List<Bet> mergeAndCheckLimits(List<Bet> rawBets) {
        if (rawBets.isEmpty()) return List.of();

        Map<BetType, Long> mergedMap = new EnumMap<>(BetType.class);

        for (Bet bet : rawBets) {
            mergedMap.merge(bet.type(), bet.value(), Long::sum);
        }

        long totalProposedAmount = mergedMap.values().stream().mapToLong(Long::longValue).sum();
        double scalingFactor = 1.0;

        long balance = getVirtualBalance();
        if (totalProposedAmount > balance) {
            scalingFactor = (double) balance / totalProposedAmount;
        }

        if (totalProposedAmount > getConfig().tableLimit()) {
            double limitFactor = (double) getConfig().tableLimit() / totalProposedAmount;
            scalingFactor = Math.min(scalingFactor, limitFactor);
        }

        final double finalScale = scalingFactor;

        return mergedMap.entrySet().stream()
                .map(entry -> {
                    long val = (long) (entry.getValue() * finalScale);
                    if (val > getMaxBet()) val = getMaxBet();
                    return new Bet(entry.getKey(), val);
                })
                .filter(b -> b.value() >= getMinBet())
                .toList();
    }

    private static class StrategyTracker {
        final IStrategy strategy;
        double weight = 1.0;
        boolean isActive = true;
        int consecutiveLosses = 0;
        List<Bet> lastVirtualBets = List.of();

        public static StrategyTracker of(IStrategy strategy) {
            return new StrategyTracker(strategy);
        }

        StrategyTracker(IStrategy strategy) {
            this.strategy = strategy;
        }
    }
}