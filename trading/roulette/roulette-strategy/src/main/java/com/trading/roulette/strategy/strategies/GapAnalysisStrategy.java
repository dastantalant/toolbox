package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.utils.BetHelper;

import java.util.*;

/**
 * Стратегия основана на статье про "Теннис 37/38".
 * Логика:
 * 1. Отслеживаем Gap (количество невыпадений подряд) для каждого числа.
 * 2. Если Gap > threshold (например, 34 или 50), число попадает в "лист наблюдения".
 * 3. Ждем virtualWait ходов (например, 4 хода), не делая реальных ставок.
 * 4. Если число так и не выпало, начинаем ставить (activeBetting).
 */
@Strategy(tags = {Tag.STATISTICAL})
public class GapAnalysisStrategy extends AbstractStrategy {

    // Настройки из статьи (можно вынести в Config при желании)
    private static final int GAP_THRESHOLD = 34; // Число не выпадало 34 хода
    private static final int VIRTUAL_WAIT = 4;   // Ждем еще 4 хода "в уме"
    private static final int BET_ATTEMPTS = 6;   // Пытаемся ставить 6 раз подряд

    // Состояние стратегии
    private final Map<Spot, Integer> currentGaps;
    private final Map<Spot, Integer> trackingWaitCounter; // Сколько ходов уже ждем
    private final Map<Spot, Integer> bettingAttemptsCounter; // Сколько раз уже поставили

    public GapAnalysisStrategy(Config config) {
        super(config);
        this.currentGaps = new EnumMap<>(Spot.class);
        this.trackingWaitCounter = new EnumMap<>(Spot.class);
        this.bettingAttemptsCounter = new EnumMap<>(Spot.class);

        // Инициализация нулями
        for (Spot spot : Spot.getAvailableList(config.rouletteType())) {
            currentGaps.put(spot, 0);
        }
    }

    @Override
    public List<Bet> decide() {
        // Ищем кандидатов, которые прошли фазу ожидания и сейчас в фазе активных ставок
        List<Spot> candidates = bettingAttemptsCounter.entrySet().stream()
                .filter(entry -> entry.getValue() < BET_ATTEMPTS) // Еще есть попытки
                .map(Map.Entry::getKey)
                .toList();

        if (candidates.isEmpty()) {
            return List.of();
        }

        long balance = getVirtualBalance();
        long betSize = getMinBet();
        // Рассчитываем ставку так, чтобы хватило на всех кандидатов
        // Если кандидатов много, можно применить Мартингейл или флэт
        // В статье автор пишет про коэффициент 5, возможно имеет смысл увеличивать ставку
        // Здесь используем простую ставку

        List<Bet> bets = new ArrayList<>();
        for (Spot spot : candidates) {
            // Проверка на лимиты баланса
            if (balance < betSize) break;

            bets.add(Bet.of(BetHelper.straightUp(spot), betSize));
            balance -= betSize;

            // Увеличиваем счетчик попыток (мы сделали ставку)
            bettingAttemptsCounter.merge(spot, 1, Integer::sum);
        }

        return bets;
    }

    @Override
    public void onRoundCustom(Spot outcome, List<Bet> bets) {
        // 1. Обновляем Гэпы (Gaps)
        List<Spot> allSpots = Spot.getAvailableList(getConfig().rouletteType());

        for (Spot spot : allSpots) {
            if (spot == outcome) {
                // Число выпало - сброс всех счетчиков
                currentGaps.put(spot, 0);
                trackingWaitCounter.remove(spot);
                bettingAttemptsCounter.remove(spot);
            } else {
                // Число не выпало - увеличиваем гэп
                currentGaps.merge(spot, 1, Integer::sum);
                processTracking(spot);
            }
        }
    }

    private void processTracking(Spot spot) {
        int gap = currentGaps.get(spot);

        // Фаза 1: Обнаружение длинного невыпадения
        if (gap >= GAP_THRESHOLD) {
            // Если число еще не отслеживается и не в ставках, начинаем следить
            if (!trackingWaitCounter.containsKey(spot) && !bettingAttemptsCounter.containsKey(spot)) {
                trackingWaitCounter.put(spot, 0);
            }
        }

        // Фаза 2: Виртуальное ожидание (Virtual Wait)
        if (trackingWaitCounter.containsKey(spot)) {
            int waited = trackingWaitCounter.get(spot);
            if (waited < VIRTUAL_WAIT) {
                trackingWaitCounter.put(spot, waited + 1);
            } else {
                // Ожидание закончено, переводим в активные ставки
                trackingWaitCounter.remove(spot);
                bettingAttemptsCounter.put(spot, 0);
            }
        }
    }
}