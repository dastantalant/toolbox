package com.trading.roulette.api.logic;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

public class KellyMoneyManager implements MoneyManager {
    private final long minBet;
    private final long maxBet;
    private final double totalSlots;
    private final double kellyFraction;

    public KellyMoneyManager(Config config) {
        minBet = config.minBet();
        maxBet = config.maxBet();
        totalSlots = Spot.availableList(config.rouletteType()).size() + (config.rouletteType().name().contains("AMERICAN") ? 1 : 0);
        kellyFraction = (config.moneyConfig().multiplier() != null)
                ? config.moneyConfig().multiplier()
                : 1.0;
    }

    @Override
    public long calculateAmount(BetType type, State state) {
        long bankroll = state.getVirtualBalance();
        if (bankroll <= 0) return 0;
        double b = type.payout();
        double p = type.winning().size() / totalSlots;
        double q = 1.0 - p;
        double f = (b * p - q) / b;
        if (f <= 0) return 0;
        long betSize = (long) (bankroll * f * kellyFraction);
        if (betSize < minBet) return minBet;
        return Math.min(betSize, maxBet);
    }

    @Override
    public void update(Spot spot, List<Bet> bets, State state) {
    }
}