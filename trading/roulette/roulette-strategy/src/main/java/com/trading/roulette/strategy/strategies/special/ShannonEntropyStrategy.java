package com.trading.roulette.strategy.strategies.special;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.utils.BetHelper;
import com.trading.roulette.api.utils.HistoryUtil;

import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Стратегия "Энтропия Шеннона".
 * <p>
 * Анализирует хаотичность выпадения чисел.
 * 1. Вычисляет энтропию последних N спинов.
 * 2. Если энтропия НИЗКАЯ -> значит система упорядочена (много повторов, горячие сектора). Играем по тренду (Repeat).
 * 3. Если энтропия ВЫСОКАЯ -> хаос. Играем на возврат к среднему (Mean Reversion) или пропускаем.
 */
@Strategy(tags = {Tag.STATISTICAL})
public class ShannonEntropyStrategy extends AbstractStrategy {

    private final int windowSize = 24;
    private final double lowEntropyThreshold = 4.0; // Для окна 24 макс энтропия ~4.5

    public ShannonEntropyStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        Deque<Spot> history = getMarket().getHistory();
        if (history.size() < windowSize) return List.of();

        List<Spot> window = HistoryUtil.window(history, windowSize);

        double entropy = calculateEntropy(window);

        // Режим низкой энтропии: рынок предсказуем, числа повторяются.
        if (entropy < lowEntropyThreshold) {
            return playHotNumbers(window);
        }

        // Режим высокой энтропии: рынок случаен.
        // В данном примере - воздерживаемся от ставок или ставим на простые шансы.
        return List.of();
    }

    private double calculateEntropy(List<Spot> spots) {
        Map<Spot, Integer> freq = new EnumMap<>(Spot.class);
        for (Spot s : spots) freq.merge(s, 1, Integer::sum);

        double entropy = 0.0;
        int n = spots.size();

        for (Integer count : freq.values()) {
            double p = (double) count / n;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    private List<Bet> playHotNumbers(List<Spot> window) {
        Map<Spot, Integer> freq = new EnumMap<>(Spot.class);
        window.forEach(s -> freq.merge(s, 1, Integer::sum));

        return freq.entrySet().stream()
                .filter(e -> e.getValue() >= 2)
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(3)
                .map(e -> Bet.of(BetHelper.straightUp(e.getKey()), getMinBet()))
                .toList();
    }
}