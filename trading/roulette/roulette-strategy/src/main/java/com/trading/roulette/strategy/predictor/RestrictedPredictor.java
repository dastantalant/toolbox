package com.trading.roulette.strategy.predictor;

import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.BetTypePrediction;
import com.trading.roulette.api.model.ColorPrediction;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.Predictor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Предиктор-обертка, реализующий логику "Инсайда" или жестких ограничений.
 * Позволяет динамически запрещать (Blacklist) или разрешать (Whitelist) конкретные числа.
 * <p>
 * Применяется, когда есть прогноз "не ставить на 4, 10, 15" или "ставить только на 0 и 32".
 */
public class RestrictedPredictor implements Predictor {

    private final Predictor delegate;

    /**
     * Набор запрещенных чисел (Blacklist). Если не пуст, эти числа исключаются из прогноза.
     */
    private Set<Spot> bannedSpots = new HashSet<>();

    /**
     * Набор разрешенных чисел (Whitelist). Если не пуст, прогноз строится ТОЛЬКО из этих чисел.
     * Имеет приоритет над bannedSpots (если whitelist задан, banned игнорируется).
     */
    private Set<Spot> allowedSpots = new HashSet<>();

    public RestrictedPredictor() {
        // По умолчанию используем случайный предиктор, если нет другой логики
        this.delegate = new RandomPredictor();
    }

    public RestrictedPredictor(Predictor delegate) {
        this.delegate = delegate;
    }

    /**
     * Возвращает прогнозы, отфильтрованные согласно спискам запретов/разрешений.
     */
    @Override
    public List<SpotPrediction> predictSpot(State state, Config config) {
        List<SpotPrediction> rawPredictions = delegate.predictSpot(state, config);

        if (rawPredictions.isEmpty()) return List.of();

        return rawPredictions.stream()
                .filter(p -> isAllowed(p.spot()))
                .toList();
    }

    @Override
    public List<BetTypePrediction> predictType(State state, Config config) {
        // Фильтрация ставок на типы (Red/Black и т.д.) сложнее,
        // так как нужно проверять, не перекрывают ли они запрещенные числа.
        // Для простоты возвращаем как есть, либо можно реализовать логику исключения.
        return delegate.predictType(state, config);
    }

    @Override
    public ColorPrediction predictColor(State state, Config config) {
        return delegate.predictColor(state, config);
    }

    /**
     * Проверяет, допустимо ли число для ставки.
     *
     * @param spot проверяемое число
     * @return true, если число можно играть
     */
    private boolean isAllowed(Spot spot) {
        // Логика Whitelist (приоритет)
        if (!allowedSpots.isEmpty()) {
            return allowedSpots.contains(spot);
        }
        // Логика Blacklist
        if (!bannedSpots.isEmpty()) {
            return !bannedSpots.contains(spot);
        }
        return true;
    }

    /**
     * Утилита для быстрой установки запрещенных чисел
     */
    public void ban(Spot... spots) {
        this.bannedSpots = new HashSet<>(Arrays.asList(spots));
    }

    /**
     * Утилита для быстрой установки разрешенных чисел
     */
    public void allowOnly(Spot... spots) {
        this.allowedSpots = new HashSet<>(Arrays.asList(spots));
    }

    /**
     * Сброс всех ограничений
     */
    public void clearRestrictions() {
        this.bannedSpots.clear();
        this.allowedSpots.clear();
    }

    public void setBannedSpots(Set<Spot> bannedSpots) {
        this.bannedSpots = bannedSpots;
    }

    public void setAllowedSpots(Set<Spot> allowedSpots) {
        this.allowedSpots = allowedSpots;
    }
}