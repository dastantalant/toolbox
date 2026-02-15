package com.trading.roulette.api.runner;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.exception.StrategyException;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.Snapshot;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public interface Runner {

    static Runner of(IStrategy strategy) {
        return new Base(strategy);
    }

    static Runner from(Config config, Class<IStrategy> clazz) {
        try {
            return new Base(clazz.getConstructor(Config.class).newInstance(config));
        } catch (Exception e) {
            throw new StrategyException("Not constructor with config", e);
        }
    }

    List<Bet> prepareBets();

    void setAcceptedBets(List<Bet> acceptedBets);

    Snapshot commitResult(Spot spot);

    default Snapshot victory(Spot spot) {
        prepareBets();
        return commitResult(spot);
    }

    Snapshot snapshot();

    boolean isLive();

    State getState();

    Snapshot stop();

    IStrategy getStrategy();

    long getBalance();

    void syncBalance(long balance);

    class Base implements Runner {
        private final UUID id = UUID.randomUUID();

        private final IStrategy strategy;
        private Snapshot snapshot;
        private boolean isLive;
        private List<Bet> plannedBets = Collections.emptyList();
        private List<Bet> acceptedBets = null;

        public Base(IStrategy strategy) {
            this.strategy = strategy;
            isLive = strategy.isLive();
        }

        @Override
        public List<Bet> prepareBets() {
            if (!isLive()) return Collections.emptyList();
            plannedBets = strategy.calculateNextBets();
            acceptedBets = null;
            return plannedBets;
        }

        public void setAcceptedBets(List<Bet> realBets) {
            acceptedBets = realBets;
        }

        @Override
        public Snapshot commitResult(Spot spot) {
            if (!isLive()) return strategy.snapshot();

            List<Bet> betsToCommit = (acceptedBets != null) ? acceptedBets : plannedBets;

            snapshot = strategy.commitResult(spot, betsToCommit);
            plannedBets = Collections.emptyList();
            acceptedBets = null;

            isLive = strategy.isLive();
            if (!isLive) stop();
            return snapshot;
        }

        @Override
        public Snapshot snapshot() {
            snapshot = strategy.snapshot();
            return snapshot;
        }

        @Override
        public boolean isLive() {
            isLive = strategy.isLive();
            return isLive;
        }

        @Override
        public State getState() {
            return strategy.getState();
        }

        @Override
        public Snapshot stop() {
            snapshot = strategy.endGame();
            return snapshot;
        }

        @Override
        public IStrategy getStrategy() {
            return strategy;
        }

        @Override
        public long getBalance() {
            return strategy.getVirtualBalance();
        }

        @Override
        public void syncBalance(long balance) {
            strategy.setVirtualBalance(balance);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public final boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Base that = (Base) obj;
            return Objects.equals(id, that.id);
        }
    }

}
