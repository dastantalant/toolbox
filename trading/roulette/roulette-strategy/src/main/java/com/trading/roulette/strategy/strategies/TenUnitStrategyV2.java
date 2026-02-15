package com.trading.roulette.strategy.strategies;


import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

    /**
     * Улучшенная версия TenUnitStrategy.
     * <p>
     * Улучшения:
     * 1. <strong>Safety Breaker (Предохранитель):</strong> Если последовательность ставок становится слишком длинной
     * (рынок идет против нас), мы не пытаемся отыграться любой ценой, а сбрасываем цикл, фиксируя убыток.
     * Это предотвращает глубокие просадки.
     * 2. <strong>Динамический юнит:</strong> Размер базовой ставки (Unit) пересчитывается при каждом сбросе
     * цикла в зависимости от текущего баланса, чтобы не рисковать фиксированной суммой при просадке.
     */
    @Strategy(tags = {Tag.PROGRESSIVE, Tag.STATISTICAL})
    public class TenUnitStrategyV2 extends AbstractStrategy {

        private final BetType type = BetType.RED; // Можно сделать адаптивным
        private final int targetUnits = 10;
        private final int maxSequenceLength = 12; // Если в списке больше 12 чисел - сброс (Stop Loss)

        private final Deque<Long> sequence = new ArrayDeque<>();
        private long currentUnitSize;
        private long sessionTargetBalance;
        private boolean isInSession;

        public TenUnitStrategyV2(Config config) {
            super(config);
            resetSession(config.startBalance());
        }

        @Override
        public List<Bet> decide() {
            long balance = getVirtualBalance();

            // 1. Проверка завершения сессии (Цель достигнута)
            if (isInSession && balance >= sessionTargetBalance) {
                resetSession(balance);
            }

            // 2. Логика обновления последовательности по результату прошлого раунда
            if (isLastBet()) {
                long lastBetVal = getLastBet();
                if (isLastBetWon()) {
                    // Победа: убираем края (первое и последнее число)
                    if (!sequence.isEmpty()) sequence.removeFirst();
                    if (!sequence.isEmpty()) sequence.removeLast();
                } else {
                    // Поражение: добавляем проигранную сумму в конец
                    sequence.addLast(lastBetVal);
                }
            }

            // 3. Safety Breaker: Если серия затянулась -> Сброс (принимаем убыток)
            if (sequence.size() > maxSequenceLength) {
                resetSession(balance);
            }

            // 4. Если последовательность пуста (все отыграли) -> Начинаем новую
            if (sequence.isEmpty()) {
                initializeSequence();
            }

            // 5. Расчет следующей ставки (сумма краев)
            long nextBet = sequence.getFirst();
            if (sequence.size() > 1) {
                nextBet += sequence.getLast();
            }

            // 6. Проверки лимитов
            nextBet = Math.clamp(nextBet, getMinBet(), getMaxBet());
            nextBet = Math.min(nextBet, balance);

            if (nextBet < getMinBet()) return List.of();

            return Bet.list(type, nextBet);
        }

        private void resetSession(long currentBalance) {
            // Пересчитываем юнит: 0.1% от баланса, но не меньше минимума
            long calculatedUnit = Math.max(getMinBet(), currentBalance / 1000);
            this.currentUnitSize = calculatedUnit;

            // Цель: +10 юнитов
            this.sessionTargetBalance = currentBalance + (targetUnits * currentUnitSize);
            this.isInSession = true;

            initializeSequence();
        }

        private void initializeSequence() {
            sequence.clear();
            // Заполняем последовательность единицами (1 1 1 ... 1)
            for (int i = 0; i < targetUnits; i++) {
                sequence.add(currentUnitSize);
            }
        }
    }
