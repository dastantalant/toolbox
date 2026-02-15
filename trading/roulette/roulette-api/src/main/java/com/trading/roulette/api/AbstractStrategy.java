package com.trading.roulette.api;

import com.toolbox.core.util.StringUtil;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.logic.MoneyManager;
import com.trading.roulette.api.logic.MoneyManagerFactory;
import com.trading.roulette.api.logic.SignalFactory;
import com.trading.roulette.api.logic.SignalGenerator;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.Market;
import com.trading.roulette.api.model.Metric;
import com.trading.roulette.api.model.Snapshot;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.risk.RiskManager;
import com.trading.roulette.api.stop.StopCondition;
import com.trading.roulette.api.stop.StopConditionFactory;
import com.trading.roulette.api.utils.BetHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractStrategy implements IStrategy {
    private final String name;
    private final List<StopCondition> stopConditions;
    private final RiskManager riskManager;
    private final Metric metric;
    private final State state;
    protected final SignalGenerator signalGenerator;
    protected final MoneyManager moneyManager;

    protected AbstractStrategy(Config config) {
        this(config, "");
    }

    protected AbstractStrategy(Config config, String name) {
        this.name = StringUtil.isBlank(name) ? getClass().getSimpleName() : name;
        stopConditions = StopConditionFactory.from(config.stopConfig(), config.startBalance());
        riskManager = RiskManager.from(config);
        metric = Metric.from(this.name, config.startBalance());
        moneyManager = MoneyManagerFactory.create(config);
        signalGenerator = SignalFactory.create(config);
        state = warmingUp(config);
        state.setLive(true);
    }

    protected State warmingUp(Config config) {
        Market market = new Market();
        for (Integer number : config.initial()) {
            try {
                Spot spot = Spot.byNumber(number);
                market.addSpot(spot);
            } catch (Exception _) {
            }
        }
        return State.of(config, market);
    }

    public final List<Bet> calculateNextBets() {
        shouldStopConditions();
        if (!isLive()) return Collections.emptyList();
        riskManager.calculateMin(this);
        long max = riskManager.calculateMax(this);
        if (max == 0) return Collections.emptyList();
        List<Bet> rawBets = decide();
        return riskManager.apply(rawBets, this);
    }

    @Override
    public List<Bet> decide() {
        List<BetType> signals = decideSignals();
        if (signals.isEmpty()) return Collections.emptyList();
        List<Bet> bets = new ArrayList<>(signals.size());
        for (BetType type : signals) {
            long amount = moneyManager.calculateAmount(type, state);
            if (amount > 0) bets.add(Bet.of(type, amount));
        }
        return bets;
    }

    protected List<BetType> decideSignals() {
        if (signalGenerator != null) {
            return signalGenerator.generate(state);
        }
        return List.of();
    }

    public final long getVirtualBalance() {
        return state.getVirtualBalance();
    }

    public final void setVirtualBalance(long balance) {
        state.setVirtualBalance(balance);
        riskManager.updateState(balance);
    }

    public final Snapshot commitResult(Spot spot, List<Bet> bets) {
        Snapshot snapshot = update(spot, bets);
        if (moneyManager != null) moneyManager.update(spot, bets, state);
        onRoundCustom(spot, bets);
        setLastBets(bets);
        return publish(snapshot);
    }

    private void shouldStopConditions() {
        for (StopCondition condition : stopConditions) {
            boolean should = condition.should(this);
            if (should) {
                endGame();
                break;
            }
        }
    }

    protected void onRoundCustom(Spot spot, List<Bet> bets) {
    }

    private Snapshot update(Spot spot, List<Bet> bets) {
        long oldBalance = getVirtualBalance();
        state.getMarket().addSpot(spot);
        if (bets == null || bets.isEmpty()) {
            metric.updateEmpty(getVirtualBalance());
            return snapshot(oldBalance, spot, bets);
        }
        long totalBetValue = BetHelper.totalBetValue(bets);
        long totalPayout = BetHelper.totalPayout(bets, spot);
        setVirtualBalance(getVirtualBalance() - totalBetValue + totalPayout);
        metric.update(getVirtualBalance(), totalBetValue, totalPayout);
        return metric.snapshot(isLive(), bets, spot, getVirtualBalance(), oldBalance, getConfig().debug());
    }

    @Override
    public Snapshot snapshot() {
        return snapshot(getVirtualBalance(), null, List.of());
    }

    protected final Snapshot snapshot(long oldBalance, Spot spot, List<Bet> bets) {
        return metric.snapshot(isLive(), bets, spot, getVirtualBalance(), oldBalance, true);
    }

    protected final Snapshot snapshot(long oldBalance, Spot spot, List<Bet> bets, boolean detailed) {
        return metric.snapshot(isLive(), bets, spot, getVirtualBalance(), oldBalance, detailed);
    }

    public final Config getConfig() {
        return state.getConfig();
    }

    public final List<Bet> getLastBets() {
        return state.getLastBets();
    }

    public final boolean isLive() {
        return state.isLive();
    }

    public final State getState() {
        return state;
    }

    protected final boolean isLastBet() {
        List<Bet> lastBets = getLastBets();
        return lastBets != null && !lastBets.isEmpty();
    }

    protected final long getLastBet() {
        return BetHelper.totalBetValue(getLastBets());
    }

    protected final void setLastBets(List<Bet> lastBets) {
        state.setLastBets(lastBets);
    }

    protected final boolean isLastBetWon() {
        Spot lastSpot = state.getMarket().getLastSpot();
        List<Bet> lastBets = getLastBets();
        if (lastBets == null || lastBets.isEmpty() || lastSpot == null) {
            return false;
        }
        return BetHelper.hasWin(lastBets, lastSpot);
    }

    @Override
    public final Snapshot endGame() {
        state.setLive(false);
        return publishGameEnd(snapshot());
    }

    @Override
    public void syncRealBalance(long realBalance) {
        if (state.getRealBalance() != realBalance) {
            state.setRealBalance(realBalance);
        }
    }

    public final Market getMarket() {
        return state.getMarket();
    }

    public final long getMinBet() {
        return riskManager.getMinBet();
    }

    public final long getMaxBet() {
        return riskManager.getMaxBet();
    }

    protected final List<Long> getHistoryBalance() {
        return metric.getHistoryBalance();
    }

    protected final double getAverage() {
        return metric.getAverage();
    }

    protected void resetCount() {
        metric.resetCount();
    }

    public final String getName() {
        return name;
    }

    public final long getStartBalance() {
        return metric.getStartBalance();
    }

    public final long getPeakBalance() {
        return metric.getPeakBalance();
    }

    public final double getMaxDrawdown() {
        return metric.getMaxDrawdown();
    }

    public final long getWonCount() {
        return metric.getWonCount();
    }

    public final long getLostCount() {
        return metric.getLostCount();
    }

    public final long getBetCount() {
        return metric.getBetCount();
    }

    public final long getRoundNumber() {
        return metric.getRoundNumber();
    }

    public final long getTotalWagered() {
        return metric.getTotalWagered();
    }

    public final long getTotalPayout() {
        return metric.getTotalPayout();
    }

    private final Set<Subscriber> subscribers = new CopyOnWriteArraySet<>();

    @Override
    public final void addSubscriber(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public final Snapshot publish(Snapshot snapshot) {
        subscribers.forEach(subscriber -> subscriber.onRound(snapshot));
        return snapshot;
    }

    private Snapshot publishGameEnd(Snapshot snapshot) {
        subscribers.forEach(subscriber -> subscriber.onGameEnd(snapshot));
        return snapshot;
    }

    //================identity================
    private final UUID id = UUID.randomUUID();

    public final UUID getId() {
        return id;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractStrategy that = (AbstractStrategy) obj;
        return Objects.equals(id, that.id);
    }
}
