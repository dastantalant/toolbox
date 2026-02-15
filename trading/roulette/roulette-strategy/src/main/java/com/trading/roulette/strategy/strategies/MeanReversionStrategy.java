package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

@Strategy(tags = {Tag.PATTERN, Tag.STATISTICAL})
public class MeanReversionStrategy extends AbstractStrategy {

    private final int colorStreakThreshold;
    private final double colorRateThreshold;
    private final int recentHistoryWindow;
    private long multiplayer;

    public MeanReversionStrategy(Config config) {
        super(config);
        multiplayer = 1;
        colorStreakThreshold = 4;
        colorRateThreshold = 0.70;
        recentHistoryWindow = 20;
    }

    @Override
    public List<Bet> decide() {
        Deque<Spot> history = getMarket().getHistory();
        if (history.size() < colorStreakThreshold) {
            return List.of();
        }

        var opt = processStreak(history);

        if (opt.isEmpty() && history.size() >= recentHistoryWindow) {
            opt = recent();
            if (opt.isEmpty()) {
                multiplayer = 1;
                return List.of();
            }
        }
        if (opt.isEmpty()) return List.of();

        multiplayer = calculate();
        return create(opt.get(), multiplayer);
    }

    private Optional<BetType> recent() {

        if (getMarket().getRedRate() > colorRateThreshold) {
            return Optional.of(BetType.BLACK);
        } else if (getMarket().getBlackRate() > colorRateThreshold) {
            return Optional.of(BetType.RED);
        }
        return Optional.empty();
    }

    private Optional<BetType> processStreak(Deque<Spot> history) {
        List<BetType> colors = extractLastColors(history);
        Optional<BetType> opt = Optional.empty();
        if (colors.size() >= colorStreakThreshold) {
            BetType first = colors.get(colors.size() - colorStreakThreshold);
            boolean isStreak = true;
            for (int i = 1; i < colorStreakThreshold; i++) {
                if (colors.get(colors.size() - i) != first) {
                    isStreak = false;
                    break;
                }
            }
            if (isStreak) {
                opt = Optional.of((first == BetType.RED) ? BetType.BLACK : BetType.RED);
            }
        }
        return opt;
    }

    private List<BetType> extractLastColors(Deque<Spot> history) {
        List<BetType> types = new ArrayList<>(history.size());
        for (Spot spot : history) {
            if (spot.isRed()) {
                types.add(BetType.RED);
            }
            if (spot.isBlack()) {
                types.add(BetType.BLACK);
            }
        }
        return types;
    }

    private long calculate() {
        long temp = isLastBetWon() ? Math.max(1, multiplayer - 1) : multiplayer + 1;
        multiplayer = !isLastBet() ? 1 : temp;
        return multiplayer;
    }

    private List<Bet> create(BetType type, long multiplayer) {
        long value = getMinBet() * multiplayer;
        value = Math.min(value, getVirtualBalance());
        value = Math.min(value, getMaxBet());
        return (value < getMinBet()) ? List.of() : Bet.list(type, value);
    }
}