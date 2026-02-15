package com.trading.roulette.strategy.strategies.special;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.utils.HistoryUtil;
import com.trading.roulette.strategy.predictor.RestrictedPredictor;
import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;
import com.trading.roulette.api.utils.BetHelper;

import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * Стратегия, управляемая "Инсайдом" или внешними сигналами.
 * <p>
 * Позволяет:
 * 1. Исключать "плохие" числа из игры (например, прогноз "не ставить на 4, 10, 15").
 * 2. Фокусироваться только на "хороших" числах.
 * 3. Динамически менять списки в ходе игры (через метод {@link AbstractStrategy#onRoundCustom} или внешнее управление).
 */
@Strategy(tags = {Tag.SPECIAL})
public class InsiderInfoStrategy extends AbstractStrategy {

    private final RestrictedPredictor predictor;

    public InsiderInfoStrategy(Config config) {
        super(config);
        // Оборачиваем мощный Марков-предиктор в наш ограничитель
        this.predictor = new RestrictedPredictor(new MultiOrderMarkovPredictor());

        // Пример начальной конфигурации "Инсайда" (можно менять)
        // TODO Запрещаем 0, 4, 10, 15, 27 как в примере
        this.predictor.ban(Spot.SPOT_0, Spot.SPOT_04, Spot.SPOT_10, Spot.SPOT_15, Spot.SPOT_27);
    }

    @Override
    public List<Bet> decide() {
        // Получаем прогнозы, уже очищенные от запрещенных чисел
        List<SpotPrediction> predictions = predictor.predictSpot(getState(), getConfig());

        if (predictions.isEmpty()) return List.of();

        // Берем топ-3 разрешенных числа
        return predictions.stream()
                .limit(3)
                .map(p -> Bet.of(BetHelper.straightUp(p.spot()), getMinBet()))
                .toList();
    }

    /**
     * Пример управления состоянием в ходе игры.
     * Например, если выпало "запрещенное" число (которое мы избегали),
     * можно считать инсайд неверным и сбросить фильтры или сменить тактику.
     */
    @Override
    protected void onRoundCustom(Spot spot, List<Bet> bets) {
        // Симуляция временного окна (например, каждые 100 раундов меняем "инсайд")
        long round = getBetCount();
        if (round > 0 && round % 100 == 0) {
            updateInsiderInfo(getState());
        }
    }

    private void updateInsiderInfo(State state) {
        // Здесь могла бы быть логика получения данных из API или по времени
        // Для примера: раз в 100 раундов меняем запрещенные числа на последние 5 выпавших (Hot Spots Avoidance)
        Deque<Spot> history = getMarket().getHistory();
        if (history.size() >= 5) {
            Set<Spot> last5 = Set.copyOf(HistoryUtil.window(history, 5));
            predictor.setBannedSpots(last5);
        }
    }
}