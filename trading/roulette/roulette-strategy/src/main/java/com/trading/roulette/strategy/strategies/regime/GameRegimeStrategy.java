package com.trading.roulette.strategy.strategies.regime;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

@Strategy(tags = {Tag.PATTERN, Tag.STATISTICAL})
public class GameRegimeStrategy extends AbstractRegimeStrategy {

    public GameRegimeStrategy(Config config) {
        RegimeContext build = RegimeContext.builder()
                .trendStreak(3)
                .alternatingStreak(4)
                .build();
        super(config, build);
    }

    @Override
    public List<Bet> decide() {
        if (getState().getMarket().getHistory().size() < alternatingStreak) {
            return List.of();
        }
        Regime currentRegime = detectRegime();
        return switch (currentRegime) {
            case TREND -> handleTrendRegime();
            case ALTERNATING -> handleAlternatingRegime();
            default -> List.of();
        };
    }

    private Regime detectRegime() {
        Deque<Spot> history = getState().getMarket().getHistory();
        int size = history.size();
        Optional<Regime> optional = detectTrend(history, size);
        if (optional.isPresent()) return optional.get();
        optional = detectAlternating(history, size);
        return optional.orElse(Regime.CHAOS);
    }

    private Optional<Regime> detectTrend(Deque<Spot> history, int size) {
        if (size < trendStreak) {
            return Optional.empty();
        }
        var it = history.descendingIterator();
        Spot last = it.next();
        if (last.isGreen()) return Optional.empty();
        boolean lastIsRed = last.isRed();
        boolean isTrend = true;
        int checked = 1;
        while (it.hasNext() && checked < trendStreak) {
            Spot prev = it.next();
            if (prev.isRed() != lastIsRed || prev.isGreen()) {
                isTrend = false;
                break;
            }
            checked++;
        }
        return isTrend ? Optional.of(Regime.TREND) : Optional.empty();
    }

    private Optional<Regime> detectAlternating(Deque<Spot> history, int size) {
        if (size < alternatingStreak) {
            return Optional.empty();
        }
        var it = history.descendingIterator();
        Spot current = it.next();
        if (current.isGreen()) {
            return Optional.empty();
        }
        boolean isAlternating = true;
        int checked = 1;
        while (it.hasNext() && checked < alternatingStreak) {
            Spot prev = it.next();
            if (current.isGreen() || prev.isGreen() || (current.isRed() == prev.isRed())) {
                isAlternating = false;
                break;
            }
            current = prev;
            checked++;
        }
        return isAlternating ? Optional.of(Regime.ALTERNATING) : Optional.empty();
    }

    private List<Bet> handleTrendRegime() {
        if (getMarket().getLastSpot() == null) return List.of();
        BetType betType = getMarket().getLastSpot().isRed() ? BetType.RED : BetType.BLACK;
        long betValue = isLastBetWon() ? getLastBet() + getMinBet() : getMinBet();
        return create(betType, betValue);
    }

    private List<Bet> handleAlternatingRegime() {
        if (getMarket().getLastSpot() == null) return List.of();
        BetType betType = getMarket().getLastSpot().isRed() ? BetType.BLACK : BetType.RED;
        return create(betType, getMinBet());
    }

    private List<Bet> create(BetType type, long value) {
        long safeValue = Math.min(value, getVirtualBalance());
        safeValue = Math.min(safeValue, getMaxBet());
        if (safeValue < getMinBet()) return List.of();
        return Bet.list(type, safeValue);
    }
}