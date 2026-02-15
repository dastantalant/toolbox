package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.enums.Constants;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Color;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.AbstractStrategy;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Strategy(tags = {Tag.AI})
public class QLearningAgentStrategy extends AbstractStrategy {

    private enum Action {BET_RED, BET_BLACK, WAIT}

    private record QState(Color last, int streak, int level) {
        static QState of(Color last, int streak, int level) {
            return new QState(last, streak, level);
        }
    }

    private final Map<QState, Map<Action, Double>> qTable;
    private final double learningRate;
    private final double discountFactor;
    private final double explorationRate;

    private QState lastState;
    private Action lastAction;

    public QLearningAgentStrategy(Config config) {
        super(config);
        qTable = new HashMap<>();
        learningRate = 0.1;
        discountFactor = 0.9;
        explorationRate = 0.1;
    }

    @Override
    public void onRoundCustom(Spot spot, List<Bet> betList) {
        if (lastState == null || lastAction == null) {
            return;
        }

        double reward = (isLastBetWon() ? 1.0 : -1.0);
        reward = isLastBet() ? reward : 0;

        Map<Action, Double> oldActionValues = qTable.computeIfAbsent(lastState, _ -> new HashMap<>());
        double oldQValue = oldActionValues.getOrDefault(lastAction, 0.0);
        double maxNextQValue = qTable.getOrDefault(getCurrentState(), Collections.emptyMap()).values().stream()
                .max(Double::compare).orElse(0.0);
        double newQValue = oldQValue + learningRate * (reward + discountFactor * maxNextQValue - oldQValue);
        oldActionValues.put(lastAction, newQValue);
    }

    @Override
    public List<Bet> decide() {
        lastState = getCurrentState();
        lastAction = (Constants.RANDOM.nextDouble() < explorationRate) ? Action.values()[Constants.RANDOM.nextInt(Action.values().length)] : getBestAction(lastState);
        return switch (lastAction) {
            case BET_RED -> createBet(BetType.RED, getMinBet());
            case BET_BLACK -> createBet(BetType.BLACK, getMinBet());
            case WAIT -> List.of();
        };
    }

    private Action getBestAction(QState state) {
        Map<Action, Double> actions = qTable.getOrDefault(state, Collections.emptyMap());
        return actions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Action.WAIT);
    }

    private QState getCurrentState() {
        Deque<Spot> history = getMarket().getHistory();
        if (history.isEmpty()) {
            return QState.of(Color.GREEN, 0, getBalanceLevel());
        }

        var it = history.descendingIterator();
        Spot last = it.next();
        Color lastColor = getSpotColor(last);

        int streak = 1;
        while (it.hasNext()) {
            Spot prev = it.next();
            if (getSpotColor(prev) == lastColor) {
                streak++;
                if (streak >= 5) break;
            } else {
                break;
            }
        }

        return QState.of(lastColor, Math.min(streak, 5), getBalanceLevel());
    }

    private int getBalanceLevel() {
        double ratio = getVirtualBalance() / (double) getConfig().startBalance();
        if (ratio < 0.5) return 0;
        if (ratio < 1.5) return 1;
        return 2;
    }

    private Color getSpotColor(Spot spot) {
        if (spot.isRed()) return Color.RED;
        if (spot.isBlack()) return Color.BLACK;
        return Color.GREEN;
    }

    private List<Bet> createBet(BetType type, long value) {
        long safeValue = Math.min(value, getVirtualBalance());
        return (safeValue < getMinBet()) ? List.of() : Bet.list(type, safeValue);
    }
}