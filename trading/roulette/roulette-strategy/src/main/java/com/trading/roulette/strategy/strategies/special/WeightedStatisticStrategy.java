package com.trading.roulette.strategy.strategies.special;

import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.utils.BetHelper;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.utils.HistoryUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Стратегия взвешенных ставок на основе статистики (Frequency Weighted Betting).
 * <p>
 * Логика:
 * 1. Анализирует частоту выпадения чисел.
 * 2. Вычисляет "вес" числа относительно среднего.
 * 3. Если число "горячее" (вес > 1.0), ставка на него увеличивается пропорционально весу.
 * 4. Использует классификацию для отсева "шума".
 */
@Strategy(tags = {Tag.STATISTICAL})
public class WeightedStatisticStrategy extends AbstractStrategy {

    private final int windowSize = 100; // Анализируем последние 100 спинов
    private final double weightMultiplier = 1.5; // Агрессивность увеличения ставки

    public WeightedStatisticStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        Deque<Spot> history = getMarket().getHistory();
        if (history.size() < 20) return List.of(); // Мало данных

        List<Spot> window = HistoryUtil.window(history, windowSize);

        // Считаем частоты
        Map<Spot, Long> frequencies = window.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        // Средняя частота (для нормализации)
        double avgFreq = frequencies.values().stream().mapToLong(l -> l).average().orElse(1.0);

        List<Bet> bets = new ArrayList<>();
        long baseBet = getMinBet();

        // Проходим по всем числам, которые выпадали чаще среднего
        for (Map.Entry<Spot, Long> entry : frequencies.entrySet()) {
            Spot spot = entry.getKey();
            long count = entry.getValue();

            // Если число выпадает чаще среднего (Hot)
            if (count > avgFreq) {
                // Рассчитываем множитель ставки: (ТекущаяЧастота / Средняя) * Multiplier
                double weight = ((double) count / avgFreq) * weightMultiplier;

                // Округляем до целого размера ставки
                long betValue = (long) (baseBet * weight);
                betValue = Math.min(betValue, getMaxBet());

                // Классифицируем и ставим
                if (SpotClassifier.isWorthy(spot, weight)) {
                    bets.add(Bet.of(BetHelper.straightUp(spot), betValue));
                }
            }
        }

        // Сортируем, чтобы взять только топ-5 самых тяжелых, если их много
        bets.sort((b1, b2) -> Long.compare(b2.value(), b1.value()));

        return bets.stream().limit(5).toList();
    }

    /**
     * Внутренний классификатор чисел.
     */
    private static class SpotClassifier {
        /**
         * Определяет, стоит ли число внимания на основе веса.
         * Можно добавить логику: "только Красные", "только Тир" и т.д.
         */
        static boolean isWorthy(Spot spot, double weight) {
            // Пример: ставим только если вес > 1.2 (значимое отклонение)
            return weight > 1.2;
        }
    }
}