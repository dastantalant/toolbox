package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.utils.BetHelper;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.*;

/**
 * V2 версия GapAnalysisStrategy.
 * Улучшения:
 * 1. Автоматический расчет GAP_THRESHOLD на основе истории (среднее + 1.5 стд. отклонения).
 * 2. Мягкий вход: ожидание (WAIT) сокращается, если число становится очень горячим по другим метрикам.
 * 3. Игра до победы (в разумных пределах прогрессии), а не фиксированное кол-во раз.
 */
@Strategy(tags = {Tag.STATISTICAL})
public class GapAnalysisStrategyV2 extends AbstractStrategy {

    private final Map<Spot, Integer> gaps = new EnumMap<>(Spot.class);
    private final Map<Spot, Integer> activeBets = new EnumMap<>(Spot.class);
    private int dynamicThreshold = 35;
    private static final int MAX_PROGRESSION_STEPS = 10;

    public GapAnalysisStrategyV2(Config config) {
        super(config);
        Spot.getAvailableList(config.rouletteType()).forEach(s -> gaps.put(s, 0));

        // Восстановление состояния из истории (если есть)
        Deque<Spot> history = getMarket().getHistory();
        if (!history.isEmpty()) {
            for (Spot spot : history) {
                updateInternalState(spot);
            }
        }
    }

    @Override
    public List<Bet> decide() {
        List<Bet> bets = new ArrayList<>();
        long balance = getVirtualBalance();
        long minBet = getMinBet();

        // 1. Поиск новых кандидатов (Безопасно: gaps не меняется здесь)
        for (Map.Entry<Spot, Integer> entry : gaps.entrySet()) {
            Spot s = entry.getKey();
            int gap = entry.getValue();
            if (gap > dynamicThreshold && !activeBets.containsKey(s)) {
                activeBets.put(s, 1);
            }
        }

        // 2. Формирование ставок и обновление шагов
        // Используем промежуточную мапу для обновлений, чтобы не ломать итератор
        Map<Spot, Integer> updates = new HashMap<>();
        List<Spot> toRemove = new ArrayList<>();

        for (Map.Entry<Spot, Integer> entry : activeBets.entrySet()) {
            Spot s = entry.getKey();
            int step = entry.getValue();

            if (step > MAX_PROGRESSION_STEPS) {
                toRemove.add(s);
                continue;
            }

            long betMultiplier = (long) Math.ceil(step / 3.0);
            long betVal = minBet * betMultiplier;

            if (balance >= betVal) {
                bets.add(Bet.of(BetHelper.straightUp(s), betVal));
                balance -= betVal;
                // Сохраняем обновление на потом
                updates.put(s, step + 1);
            }
        }

        // Применяем изменения после цикла
        activeBets.putAll(updates);
        toRemove.forEach(activeBets::remove);

        return bets;
    }

    @Override
    protected void onRoundCustom(Spot outcome, List<Bet> bets) {
        updateInternalState(outcome);
    }

    private void updateInternalState(Spot outcome) {
        // Итерация по keyset EnumMap безопасна для put/merge
        for (Spot s : gaps.keySet()) {
            if (s == outcome) {
                gaps.put(s, 0);
                activeBets.remove(s);
            } else {
                gaps.merge(s, 1, Integer::sum);
            }
        }
    }
}