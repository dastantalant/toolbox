package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.utils.BetHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V2 версия SmartMarkovStrategy.
 * <p>
 * Улучшения:
 * 1. <strong>Затухающая память (Decay):</strong> Старые переходы теряют вес. Это позволяет стратегии адаптироваться к изменению поведения ГСЧ/Дилера.
 * 2. <strong>Фильтр шума:</strong> Ставит только если вероятность перехода значительно выше случайной (1/37).
 * 3. <strong>Топ-1 вместо Топ-3:</strong> Концентрация бюджета на самом вероятном исходе для максимизации ROI.
 */
@Strategy(tags = {Tag.PREDICTIVE})
public class SmartMarkovStrategyV2 extends AbstractStrategy {

    // Матрица весов: Откуда -> {Куда -> Вес}
    private final Map<Spot, Map<Spot, Double>> transitionWeights = new ConcurrentHashMap<>();
    private final double decayFactor = 0.995; // Каждое обновление уменьшает старые веса на 0.5%
    private final double minProbabilityThreshold = 0.05; // Порог входа (выше 1/20)

    public SmartMarkovStrategyV2(Config config) {
        super(config);
        Deque<Spot> history = getMarket().getHistory();
        if (history.size() < 2) return;
        var iterator = history.iterator();
        Spot prev = iterator.next();
        while (iterator.hasNext()) {
            Spot curr = iterator.next();
            updateModel(prev, curr);
            prev = curr;
        }
    }

    @Override
    public List<Bet> decide() {
        Spot lastSpot = getMarket().getLastSpot();
        if (lastSpot == null) return List.of();

        Map<Spot, Double> candidates = transitionWeights.get(lastSpot);
        if (candidates == null || candidates.isEmpty()) return List.of();

        // Нормализация весов для текущего узла
        double totalWeight = candidates.values().stream().mapToDouble(d -> d).sum();

        // Ищем лучший переход
        return candidates.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> (e.getValue() / totalWeight) >= minProbabilityThreshold)
                .map(e -> {
                    // Kelly-lite сайзинг
                    double prob = e.getValue() / totalWeight;
                    long bank = getVirtualBalance();
                    long bet = (long) (bank * 0.01 * (prob * 10));
                    bet = Math.clamp(bet, getMinBet(), getMaxBet());

                    return Bet.list(BetHelper.straightUp(e.getKey()), bet);
                })
                .orElse(List.of());
    }

    @Override
    protected void onRoundCustom(Spot currentSpot, List<Bet> bets) {
        Deque<Spot> history = getMarket().getHistory();
        if (history.size() < 2) return;
        Iterator<Spot> iterator = history.descendingIterator();
        iterator.next();
        Spot previousSpot = iterator.next();

        updateModel(previousSpot, currentSpot);
    }

    private void updateModel(Spot prev, Spot curr) {
        transitionWeights.computeIfAbsent(prev, k -> new ConcurrentHashMap<>())
                .merge(curr, 1.0, (oldVal, newVal) -> (oldVal * decayFactor) + newVal);
    }
}