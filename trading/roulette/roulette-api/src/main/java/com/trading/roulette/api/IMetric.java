package com.trading.roulette.api;

import com.trading.roulette.api.model.Bet;

import java.util.List;

public interface IMetric extends Naming {

    long getMinBet();

    long getMaxBet();

    long getVirtualBalance();

    void setVirtualBalance(long balance);

    long getStartBalance();

    long getPeakBalance();

    double getMaxDrawdown();

    long getWonCount();

    long getLostCount();

    long getBetCount();

    long getRoundNumber();

    long getTotalWagered();

    long getTotalPayout();

    List<Bet> getLastBets();
}