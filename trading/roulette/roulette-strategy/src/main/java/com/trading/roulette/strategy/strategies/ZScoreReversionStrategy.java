package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.utils.BetHelper;
import com.trading.roulette.api.utils.HistoryUtil;

import java.util.*;

/**
 * НОВАЯ СТРАТЕГИЯ: Z-Score Mean Reversion (Статистический Арбитраж).
 * <p>
 * Принцип:
 * Вычисляет Z-Score (стандартное отклонение от среднего) для частоты выпадения каждого числа.
 * Если число выпадает слишком редко (Z < -2.0), статистически ожидается его возврат к среднему.
 * <p>
 * Отличие от "Холодных чисел": Использует математически обоснованный порог (сигма),
 * зависящий от размера выборки, а не просто "топ-5 редких".
 */
@Strategy(tags = {Tag.STATISTICAL})
public class ZScoreReversionStrategy extends AbstractStrategy {

    private final int sampleSize = 200; // Скользящее окно анализа
    private final double zThreshold = -2.0; // Порог входа (2 сигмы отклонения вниз)

    public ZScoreReversionStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        Deque<Spot> history = getMarket().getHistory();
        int n = Math.min(history.size(), sampleSize);
        if (n < 50) return List.of(); // Нужен разгон

        List<Spot> window = HistoryUtil.window(history, n);

        // 1. Считаем фактические частоты
        Map<Spot, Long> counts = new EnumMap<>(Spot.class);
        window.forEach(s -> counts.merge(s, 1L, Long::sum));

        // 2. Теоретическое ожидание
        double p = 1.0 / 37.0; // Для Европейской
        if (getConfig().rouletteType() == RouletteType.AMERICAN) {
            p = 1.0 / 38.0;
        }
        double expectedMean = n * p;
        double stdDev = Math.sqrt(n * p * (1 - p));

        List<Bet> bets = new ArrayList<>();
        long betValue = getMinBet();

        // 3. Расчет Z-Score для каждого числа
        for (Spot spot : Spot.getAvailableList(getConfig().rouletteType())) {
            long count = counts.getOrDefault(spot, 0L);
            double zScore = (count - expectedMean) / stdDev;

            // Если отклонение ниже -2 сигм (очень сильная недооценка)
            if (zScore < zThreshold) {
                // Дополнительный фильтр: не ставим, если оно только что выпадало (ждем гэпа)
                // Но в простейшей версии ставим сразу
                bets.add(Bet.of(BetHelper.straightUp(spot), betValue));
            }
        }

        // Лимит ставок (не более 5 чисел одновременно, чтобы не размывать банк)
        if (bets.size() > 5) {
            // Если кандидатов слишком много, значит рынок аномальный, лучше пропустить
            return List.of();
        }

        return bets;
    }
}