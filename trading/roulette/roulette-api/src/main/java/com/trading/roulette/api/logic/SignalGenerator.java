package com.trading.roulette.api.logic;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.State;

import java.util.List;

@FunctionalInterface
public interface SignalGenerator {
    List<BetType> generate(State state);
}