package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.annotation.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Strategy(tags = {Tag.COVERAGE, Tag.PROGRESSIVE})
public class MansuriansStrategy extends AbstractStrategy {
    private final List<BetType> corners;
    private final List<BetType> activeCorners;
    private int lossStreak;

    public MansuriansStrategy(Config config) {
        super(config);
        activeCorners = new ArrayList<>();
        corners = List.of(
                BetType.CORNER_1_2_4_5,
                BetType.CORNER_32_33_35_36,
                BetType.CORNER_0_1_2_3
        );
    }

    @Override
    public List<Bet> decide() {
        if (isLastBetWon() || !isLastBet()) {
            lossStreak = 0;
            activeCorners.clear();
        } else {
            lossStreak++;
        }

        // --- ИСПРАВЛЕНИЕ ЗАВИСАНИЯ ---
        // Не пытаемся поставить больше углов, чем у нас есть в наличии
        int cornersNeeded = Math.min(lossStreak + 1, 5);
        int cornersAvailable = corners.size();
        int targetCount = Math.min(cornersNeeded, cornersAvailable);

        long value = getMinBet() * 5;
        if (lossStreak < 3) {
            value = getMinBet();
        } else if (lossStreak == 3) {
            value = getMinBet() * 2;
        } else if (lossStreak == 4) {
            value = getMinBet() * 3;
        }

        // Безопасный цикл с условием выхода
        while (activeCorners.size() < targetCount) {
            Optional<BetType> next = findNextAvailableCorner();
            if (next.isPresent()) {
                activeCorners.add(next.get());
            } else {
                break; // <-- ВЫХОД, если углы кончились
            }
        }
        // -----------------------------

        List<Bet> bets = new ArrayList<>(activeCorners.size());
        for (BetType type : activeCorners) {
            bets.add(Bet.of(type, value));
        }
        return bets;
    }

    private Optional<BetType> findNextAvailableCorner() {
        for (BetType corner : corners) {
            if (!activeCorners.contains(corner)) {
                return Optional.of(corner);
            }
        }
        return Optional.empty();
    }
}