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
 * НОВАЯ СТРАТЕГИЯ: Regime Switching (Переключение режимов).
 * <p>
 * Мета-стратегия, которая определяет текущее состояние рынка:
 * 1. <strong>Trend (Hot):</strong> Числа повторяются, работают "Горячие" стратегии.
 * 2. <strong>Mean Reversion (Cold):</strong> Рынок стремится к равновесию, работают "Холодные" стратегии.
 * <p>
 * Она виртуально отслеживает эффективность обоих подходов и переключает реальные ставки на победителя.
 */
@Strategy(tags = {Tag.HYBRID})
public class RegimeSwitchingStrategy extends AbstractStrategy {

    private enum Mode { TREND, MEAN_REVERSION, NEUTRAL }

    private Mode currentMode = Mode.NEUTRAL;
    private double trendScore = 0;
    private double meanRevScore = 0;

    // Виртуальные портфели
    private final List<Spot> hotCandidates = new ArrayList<>();
    private final List<Spot> coldCandidates = new ArrayList<>();

    public RegimeSwitchingStrategy(Config config) {
        super(config);
    }

    @Override
    public List<Bet> decide() {
        // Определяем кандидатов на следующий ход
        analyzeMarket();

        long betValue = getMinBet();
        List<Bet> bets = new ArrayList<>();

        // Ставим в зависимости от того, какой режим сейчас "тащит"
        if (currentMode == Mode.TREND) {
            hotCandidates.forEach(s -> bets.add(Bet.of(BetHelper.straightUp(s), betValue)));
        } else if (currentMode == Mode.MEAN_REVERSION) {
            coldCandidates.forEach(s -> bets.add(Bet.of(BetHelper.straightUp(s), betValue)));
        }

        return bets;
    }

    @Override
    protected void onRoundCustom(Spot outcome, List<Bet> bets) {
        // Проверяем, кто был бы прав в этом раунде
        boolean trendWin = hotCandidates.contains(outcome);
        boolean meanRevWin = coldCandidates.contains(outcome);

        // Обновляем очки (Скользящее окно эффективности)
        trendScore = (trendScore * 0.95) + (trendWin ? 1.0 : -0.05);
        meanRevScore = (meanRevScore * 0.95) + (meanRevWin ? 1.0 : -0.05);

        // Переключение режимов (Гистерезис)
        if (trendScore > meanRevScore + 0.5 && trendScore > 0) {
            currentMode = Mode.TREND;
        } else if (meanRevScore > trendScore + 0.5 && meanRevScore > 0) {
            currentMode = Mode.MEAN_REVERSION;
        } else if (trendScore < 0 && meanRevScore < 0) {
            currentMode = Mode.NEUTRAL; // Оба сливают, лучше не ставить
        }
    }

    private void analyzeMarket() {
        Deque<Spot> history = getMarket().getHistory();
        if (history.isEmpty()) return;

        Map<Spot, Integer> freq = getMarket().getFrequencies();

        hotCandidates.clear();
        coldCandidates.clear();

        // Простая логика отбора:
        // Hot: Топ-3 частых за всю историю (или за окно N, если переделать MarketData)
        freq.entrySet().stream()
                .sorted(Map.Entry.<Spot, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> hotCandidates.add(e.getKey()));

        // Cold: Топ-3 редких (но не 0, чтобы исключить мертвые зоны колеса)
        freq.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.comparingByValue())
                .limit(3)
                .forEach(e -> coldCandidates.add(e.getKey()));
    }
}