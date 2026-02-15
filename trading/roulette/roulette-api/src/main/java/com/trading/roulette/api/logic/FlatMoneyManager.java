package com.trading.roulette.api.logic;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

public class FlatMoneyManager implements MoneyManager {
    private final long amount;
    public FlatMoneyManager(Config config) { this.amount = config.minBet(); }
    @Override public long calculateAmount(BetType type, State state) { return amount; }
    @Override public void update(Spot spot, List<Bet> bets, State state) {}
}
