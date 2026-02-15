package com.trading.roulette.api.risk;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

public interface RiskManager {
    List<Bet> apply(List<Bet> bets, IStrategy strategy);

    void updateState(long currentBalance);

    long calculateMin(IStrategy strategy);

    long calculateMax(IStrategy strategy);

    long getMinBet();

    long getMaxBet();

    static RiskManager from(Config config) {
        return new ZoneRiskManager(config);
    }
}