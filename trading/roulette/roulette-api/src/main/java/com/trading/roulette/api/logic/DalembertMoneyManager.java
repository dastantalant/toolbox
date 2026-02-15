package com.trading.roulette.api.logic;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.utils.BetHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DalembertMoneyManager implements MoneyManager {
    private final long baseBet;
    private final long step;
    private final long maxBet;
    private final Map<BetType, Long> currentBets = new HashMap<>();

    public DalembertMoneyManager(Config config, Long step) {
        baseBet = config.minBet();
        maxBet = config.maxBet();
        this.step = (step != null && step > 0) ? step : baseBet;
    }

    @Override
    public long calculateAmount(BetType type, State state) {
        return currentBets.getOrDefault(type, baseBet);
    }

    @Override
    public void update(Spot spot, List<Bet> bets, State state) {
        if (bets == null || bets.isEmpty()) return;
        for (Bet bet : bets) {
            boolean win = BetHelper.isWin(bet, spot);
            long current = currentBets.getOrDefault(bet.type(), baseBet);
            long next;
            if (win) {
                next = current - step;
                if (next < baseBet) next = baseBet;
            } else {
                next = current + step;
                if (next > maxBet) next = maxBet;
            }
            currentBets.put(bet.type(), next);
        }
    }
}