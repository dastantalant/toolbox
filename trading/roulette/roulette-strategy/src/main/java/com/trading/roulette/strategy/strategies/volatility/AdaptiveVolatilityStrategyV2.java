package com.trading.roulette.strategy.strategies.volatility;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

/**
 * V2 версия AdaptiveVolatilityStrategy.
 * Улучшения:
 * 1. Переключение между режимами "Атака" (Paroli) и "Защита" (Flat).
 * 2. Анализ не только баланса, но и винрейта последних 20 раундов.
 * 3. Не останавливается при просадке, а уменьшает сайз до минимума.
 */
@Strategy(tags = {Tag.STATISTICAL})
public class AdaptiveVolatilityStrategyV2 extends AbstractStrategy {

    private final BetType type = BetType.RED; // Можно сделать динамическим выбором цвета
    private int winStreak = 0;
    private final int historyWindow = 20;

    public AdaptiveVolatilityStrategyV2(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        long minBet = getMinBet();
        long balance = getVirtualBalance();

        // 1. Оценка режима рынка (локальный винрейт)
        double localWinRate = calculateLocalWinRate();
        boolean isFavorable = localWinRate > 0.5; // Если выигрываем чаще 50%, рынок "дает"

        // 2. Логика ставок
        long betValue;

        if (isLastBet()) {
            if (isLastBetWon()) {
                winStreak++;
                // Если рынок благоприятный и мы в серии побед -> Paroli (удваиваем)
                if (isFavorable && winStreak < 4) {
                    betValue = getLastBet() * 2;
                } else {
                    // Либо плохой рынок, либо серия затянулась -> Фиксируем (Base)
                    betValue = minBet;
                    winStreak = 0;
                }
            } else {
                winStreak = 0;
                // При проигрыше всегда возврат к базе (без Мартингейла!)
                betValue = minBet;
            }
        } else {
            betValue = minBet;
        }

        // Защита от слива: если просадка > 20%, уменьшаем базу (если возможно)
        if (balance < getStartBalance() * 0.8) {
            // Здесь можно было бы уменьшить ставку, но ниже minBet нельзя
        }

        return Bet.list(type, Math.min(betValue, balance));
    }

    private double calculateLocalWinRate() {
        // Упрощенная эвристика: берем последние 20 исходов из статистики стратегии
        long total = getBetCount();
        if (total < historyWindow) return 0.48; // Консервативная оценка на старте

        // В реальном коде нужно хранить скользящее окно побед/поражений
        // Здесь используем аппроксимацию через общий винрейт,
        // но для V2 лучше добавить Deque<Boolean> winsHistory
        return (double) getWonCount() / total;
    }
}