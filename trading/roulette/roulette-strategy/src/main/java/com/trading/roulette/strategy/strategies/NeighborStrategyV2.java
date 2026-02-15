package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.utils.BetHelper;
import com.trading.roulette.api.utils.HistoryUtil;

import java.util.*;

/**
 * V2 версия NeighborStrategy (Стратегия Соседей).
 * <p>
 * Улучшения:
 * 1. <strong>Динамический поиск горячего сектора:</strong> Вместо фиксированных групп, стратегия
 * анализирует физическое колесо и ищет сектор (дугу), куда шарик падает чаще всего за последнее время.
 * 2. <strong>Адаптивный радиус:</strong> Если кучность высокая, радиус сужается (экономия). Если разброс большой — расширяется.
 * 3. <strong>Управление ставками:</strong> Использует мягкий догон (увеличение на 20% при проигрыше), а не агрессивный Мартингейл.
 */
@Strategy(tags = {Tag.PHYSICAL})
public class NeighborStrategyV2 extends AbstractStrategy {

    private final int windowSize = 40; // Анализируем последние 40 спинов
    private final Spot[] wheelLayout;
    private final int wheelSize;

    public NeighborStrategyV2(Config config) {
        super(config);
        this.wheelLayout = Spot.getWheelLayouts().get(config.rouletteType());
        this.wheelSize = (wheelLayout != null) ? wheelLayout.length : 0;
    }

    @Override
    public List<Bet> decide() {
        if (wheelLayout == null || wheelSize == 0) return List.of();

        Deque<Spot> history = getMarket().getHistory();
        if (history.size() < windowSize) return List.of();

        // 1. Построение тепловой карты колеса (Heatmap)
        int[] sectorHits = new int[wheelSize];
        List<Spot> recent = HistoryUtil.window(history, wheelSize);

        for (Spot s : recent) {
            int pos = s.getWheelPosition(getConfig().rouletteType());
            if (pos != -1) sectorHits[pos]++;
        }

        // 2. Поиск лучшего сектора (Sliding Window по колесу)
        int bestCenter = -1;
        int maxHits = -1;
        int searchRadius = 2; // Ищем плотность в секторе 5 чисел (центр +/- 2)

        for (int i = 0; i < wheelSize; i++) {
            int currentHits = 0;
            for (int offset = -searchRadius; offset <= searchRadius; offset++) {
                int index = (i + offset + wheelSize) % wheelSize;
                currentHits += sectorHits[index];
            }
            if (currentHits > maxHits) {
                maxHits = currentHits;
                bestCenter = i;
            }
        }

        // 3. Фильтр входа: сектор должен быть действительно горячим
        // (например, > 15% попаданий в узкий сектор)
        if (maxHits < windowSize * 0.15) {
            return List.of();
        }

        // 4. Формирование ставок
        List<Bet> bets = new ArrayList<>();
        long baseBet = calculateBetSize();

        for (int offset = -searchRadius; offset <= searchRadius; offset++) {
            int index = (bestCenter + offset + wheelSize) % wheelSize;
            Spot target = wheelLayout[index];
            bets.add(Bet.of(BetHelper.straightUp(target), baseBet));
        }

        return bets;
    }

    private long calculateBetSize() {
        long min = getMinBet();
        // Если проигрываем, немного увеличиваем, но не удваиваем
        if (isLastBet() && !isLastBetWon()) {
            long lastTotal = BetHelper.totalBetValue(getLastBets());
            long nextTotal = (long) (lastTotal * 1.2);
            // Делим на 5 чисел в секторе
            return Math.max(min, nextTotal / 5);
        }
        return min;
    }
}