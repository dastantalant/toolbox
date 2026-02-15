package com.trading.roulette.api;

import com.trading.roulette.api.model.Bet;

import java.util.List;

@FunctionalInterface
public interface IMaker {
    List<Bet> decide();
}