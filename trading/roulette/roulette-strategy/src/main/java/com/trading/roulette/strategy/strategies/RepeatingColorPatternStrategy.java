package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Color;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.AbstractStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Strategy(tags = {Tag.PATTERN})
public class RepeatingColorPatternStrategy extends AbstractStrategy {

    private static final int MIN_HISTORY_FOR_PATTERN = 5;
    private static final int PATTERN_LENGTH = 3;

    public RepeatingColorPatternStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        if (getMarket().getHistory().size() < MIN_HISTORY_FOR_PATTERN) {
            return List.of();
        }

        List<Color> colorHistory = new ArrayList<>(getMarket().getHistory().size());
        for (Spot spot : getMarket().getHistory()) {
            if (spot.isRed()) colorHistory.add(Color.RED);
            if (spot.isBlack()) colorHistory.add(Color.BLACK);
            if (spot.isGreen()) colorHistory.add(Color.GREEN);
        }

        if (colorHistory.size() < MIN_HISTORY_FOR_PATTERN) {
            return List.of();
        }

        return findColorPattern(colorHistory).map(betType -> createBet(betType))
                .orElseGet(List::of);
    }

    public Optional<BetType> findColorPattern(List<Color> colorHistory) {
        Optional<BetType> repeatingPatternBet = checkForRepeatingPattern(colorHistory);
        if (repeatingPatternBet.isPresent()) {
            return repeatingPatternBet;
        }
        if (colorHistory.size() >= 2) {
            Color lastColor = colorHistory.getLast();
            Color secondToLastColor = colorHistory.get(colorHistory.size() - 2);
            if (lastColor != Color.GREEN && secondToLastColor != Color.GREEN) {
                return getOppositeColorBet(lastColor);
            }
        }

        return Optional.empty();
    }

    private Optional<BetType> checkForRepeatingPattern(List<Color> history) {
        int requiredSize = PATTERN_LENGTH * 2 + 1;
        if (history.size() < requiredSize) {
            return Optional.empty();
        }

        int size = history.size();
        int previousPatternStart = size - (PATTERN_LENGTH * 2);
        int lastPatternStart = size - PATTERN_LENGTH;

        List<Color> lastPattern = history.subList(lastPatternStart, size);
        List<Color> previousPattern = history.subList(previousPatternStart, lastPatternStart);

        if (lastPattern.equals(previousPattern)) {
            Color predictedColor = history.get(previousPatternStart - 1);
            return mapColorToBetType(predictedColor);
        }

        return Optional.empty();
    }

    private Optional<BetType> getOppositeColorBet(Color color) {
        return switch (color) {
            case RED -> Optional.of(BetType.BLACK);
            case BLACK -> Optional.of(BetType.RED);
            default -> Optional.empty();
        };
    }

    private Optional<BetType> mapColorToBetType(Color color) {
        return switch (color) {
            case RED -> Optional.of(BetType.RED);
            case BLACK -> Optional.of(BetType.BLACK);
            default -> Optional.empty();
        };
    }

    private List<Bet> createBet(BetType type) {
        if (type == BetType.ZERO || type == BetType.DOUBLE_ZERO) {
            return List.of();
        }
        long next = (!isLastBet() || isLastBetWon())
                ? getMinBet()
                : getLastBet() + getMinBet();
        long balance = getVirtualBalance();
        long value = Math.min(next, balance);

        if (value <= 0 && balance > 0) {
            value = getMinBet();
        } else if (value <= 0) {
            return List.of();
        }

        return Bet.list(type, value);
    }
}