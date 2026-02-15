package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.utils.HistoryUtil;

import java.util.*;

/**
 * НОВАЯ СТРАТЕГИЯ: Regime Trend Follower.
 * <p>
 * Вместо того чтобы угадывать конкретное число (что имеет высокую дисперсию),
 * стратегия отслеживает тренды простых шансов (Красное/Черное, Чет/Нечет) и Дюжин.
 * <p>
 * Логика:
 * 1. Считает "силу тренда" для каждого типа ставки за последние N спинов.
 * 2. Если тренд сильный (например, красное выпало 7 раз из 10), ставит по тренду.
 * 3. Использует флэт-беттинг (фиксированная ставка), чтобы минимизировать риски.
 */
@Strategy(tags = {Tag.STATISTICAL, Tag.PATTERN})
public class RegimeTrendFollowerStrategy extends AbstractStrategy {

    private static final int WINDOW = 12; // Окно анализа
    private static final double TREND_THRESHOLD = 0.65; // Порог входа (65% выпадений)

    // Отслеживаемые типы ставок (взаимоисключающие пары/тройки)
    private final List<BetType> simpleChances = List.of(
            BetType.RED, BetType.BLACK,
            BetType.EVEN, BetType.ODD,
            BetType.ONE_TO_EIGHTEEN, BetType.NINETEEN_TO_THIRTY_SIX
    );

    private final List<BetType> dozens = List.of(
            BetType.FIRST_DOZEN, BetType.SECOND_DOZEN, BetType.THIRD_DOZEN
    );

    public RegimeTrendFollowerStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        Deque<Spot> history = getState().getMarket().getHistory();
        if (history.size() < WINDOW) return List.of();

        List<Spot> recent = HistoryUtil.window(history, WINDOW);
        List<Bet> bets = new ArrayList<>();
        long baseBet = getMinBet();

        // 1. Анализ простых шансов
        for (BetType type : simpleChances) {
            double rate = calculateWinRate(recent, type);
            if (rate >= TREND_THRESHOLD) {
                bets.add(Bet.of(type, baseBet));
            }
        }

        // 2. Анализ дюжин (порог ниже, т.к. вероятность 1/3)
        // Если дюжина выпадает чаще 50% (при норме 33%), это сильный тренд
        for (BetType type : dozens) {
            double rate = calculateWinRate(recent, type);
            if (rate >= 0.50) {
                bets.add(Bet.of(type, baseBet));
            }
        }

        // Защита от перегрузки ставками (не ставить на все сразу)
        if (bets.size() > 3) {
            // Если сигналов слишком много, рынок хаотичен -> не ставим или берем топ-1
            return List.of();
        }

        return bets;
    }

    private double calculateWinRate(List<Spot> spots, BetType type) {
        long wins = spots.stream()
                .filter(s -> type.getWinning().contains(s))
                .count();
        return (double) wins / spots.size();
    }
}