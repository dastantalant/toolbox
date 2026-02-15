package com.trading.roulette.api.logic;


import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.utils.BetHelper;

import java.util.List;

public class MartingaleMoneyManager implements MoneyManager {
    private final long baseBet;
    private final long maxBet;
    private final double multiplier;
    private long currentBet;

    public MartingaleMoneyManager(Config config, Double multiplier) {
        this.baseBet = config.minBet();
        this.maxBet = config.maxBet();
        this.multiplier = (multiplier != null && multiplier > 1) ? multiplier : 2.0;
        this.currentBet = baseBet;
    }

    @Override
    public long calculateAmount(BetType type, State state) {
        return currentBet;
    }

    @Override
    public void update(Spot spot, List<Bet> bets, State state) {
        if (bets.isEmpty()) return;
        boolean win = BetHelper.hasWin(bets, spot);
        if (win) {
            currentBet = baseBet;
        } else {
            currentBet = (long) (currentBet * multiplier);
            if (currentBet > maxBet) currentBet = maxBet;
        }
    }
}