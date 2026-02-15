package com.trading.roulette.api;

import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.Snapshot;
import com.trading.roulette.api.model.State;

import java.util.List;

public interface IStrategy extends IMetric, IMaker, Publisher {

    List<Bet> calculateNextBets();

    Snapshot commitResult(Spot spot, List<Bet> actualBets);

    default Snapshot victory(Spot spot) {
        List<Bet> bets = calculateNextBets();
        return commitResult(spot, bets);
    }

    State getState();

    Config getConfig();

    boolean isLive();

    Snapshot snapshot();

    void syncRealBalance(long realBalance);

    Snapshot endGame();
}
