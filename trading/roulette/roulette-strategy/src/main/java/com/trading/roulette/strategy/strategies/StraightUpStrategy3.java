package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.utils.BetHelper;
import com.trading.roulette.api.utils.HistoryUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Strategy
public class StraightUpStrategy3 extends AbstractStrategy {

    private int count;

    public StraightUpStrategy3(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        count = isLastBetWon() ? 0 : count + 1;
        Set<Spot> excluded = new HashSet<>(HistoryUtil.window(getMarket().getHistory(), count));
        return Spot.availableList(getConfig().rouletteType())
                .stream()
                .filter(spot -> !excluded.contains(spot))
                .map(spot -> BetHelper.betting(spot, getMinBet()))
                .toList();
    }
}