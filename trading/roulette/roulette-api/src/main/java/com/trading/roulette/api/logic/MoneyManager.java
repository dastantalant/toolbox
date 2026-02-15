package com.trading.roulette.api.logic;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.State;

import java.util.List;

public interface MoneyManager {

    long calculateAmount(BetType type, State state);

    void update(Spot spot, List<Bet> bets, State state);
}
