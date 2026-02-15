package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.utils.BetHelper;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Strategy(tags = {Tag.PHYSICAL})
public class NeighborAnalysisStrategy extends AbstractStrategy {

    private final int neighborDistanceThreshold;
    private final int historyForTrendAnalysis;

    public NeighborAnalysisStrategy(Config config) {
        this(config, 3, 10);
    }

    public NeighborAnalysisStrategy(Config config, int neighborDistanceThreshold, int historyForTrendAnalysis) {
        super(config);
        this.neighborDistanceThreshold = neighborDistanceThreshold;
        this.historyForTrendAnalysis = historyForTrendAnalysis;
    }

    @Override
    public List<Bet> decide() {
        Deque<Spot> history = getMarket().getHistory();
        if (history.size() < 2) {
            return List.of();
        }

        if (neighbor(history) > 0.5) {
            return nextInternal(history.getLast());
        }
        return List.of();
    }

    private double neighbor(Deque<Spot> history) {
        if (history.size() < 2) return 0.0;

        long neighborHits = 0;
        int window = Math.min(history.size() - 1, historyForTrendAnalysis);

        var it = history.descendingIterator();
        Spot prev = it.next();  // самый последний

        for (int i = 0; i < window && it.hasNext(); i++) {
            Spot cur = it.next();
            try {
                if (cur.getPhysicalDistance(prev, getConfig().rouletteType()) <= neighborDistanceThreshold) {
                    neighborHits++;
                }
            } catch (IllegalArgumentException e) {
                // skip
            }
            prev = cur;  // для следующей итерации
        }

        return window == 0 ? 0.0 : (double) neighborHits / window;
    }

    private List<Bet> nextInternal(Spot last) {
        List<Bet> bets = new ArrayList<>();
        RouletteType rType = getConfig().rouletteType();
        Spot[] spots = Spot.getWheelLayouts().get(rType);
        if (spots == null) {
            return List.of();
        }

        int position = last.getWheelPosition(rType);
        if (position == -1) {
            return List.of();
        }

        bets.add(Bet.of(BetHelper.straightUp(last), getMinBet()));
        for (int i = 1; i <= neighborDistanceThreshold; i++) {
            Spot left = spots[(position - i + spots.length) % spots.length];
            if (left.getWheelPosition(rType) != -1) {
                bets.add(Bet.of(BetHelper.straightUp(left), getMinBet()));
            }
            Spot right = spots[(position + i) % spots.length];
            if (right.getWheelPosition(rType) != -1 && !left.equals(right)) {
                bets.add(Bet.of(BetHelper.straightUp(right), getMinBet()));
            }
        }
        return bets;
    }
}