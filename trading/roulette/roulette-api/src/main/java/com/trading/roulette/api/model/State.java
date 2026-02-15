package com.trading.roulette.api.model;

import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.config.Config;

import java.util.Collections;
import java.util.List;

public class State {
    private final Market market;
    private final Config config;

    private long realBalance;
    private long virtualBalance;
    private List<Bet> lastBets;
    private boolean live;

    public State(Config config, Market market) {
        this.market = market;
        this.config = config;
        virtualBalance = config.startBalance();
        realBalance = config.realBalance();
        lastBets = Collections.emptyList();
        live = true;
    }

    public static State of(Config config, Market market) {
        return new State(config, market);
    }

    public Spot registerSpot(Spot spot) {
        market.addSpot(spot);
        return spot;
    }

    public final Config getConfig() {
        return config;
    }

    public final boolean isLive() {
        return live;
    }

    public final void setLive(boolean live) {
        this.live = live;
    }

    public final List<Bet> getLastBets() {
        return lastBets;
    }

    public final void setLastBets(List<Bet> lastBets) {
        this.lastBets = lastBets;
    }

    public Market getMarket() {
        return market;
    }

    public long getVirtualBalance() {
        return virtualBalance;
    }

    public void setVirtualBalance(long balance) {
        virtualBalance = balance;
    }

    public long getRealBalance() {
        return realBalance;
    }

    public void setRealBalance(long balance) {
        realBalance = balance;
    }
}