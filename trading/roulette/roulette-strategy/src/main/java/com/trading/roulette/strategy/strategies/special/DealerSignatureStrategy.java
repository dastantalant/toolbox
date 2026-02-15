package com.trading.roulette.strategy.strategies.special;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.strategy.predictor.SectorMomentumPredictor;
import com.trading.roulette.api.utils.BetHelper;

import java.util.List;

/**
 * Стратегия "Почерк Дилера" (Dealer Signature).
 * <p>
 * Использует {@link SectorMomentumPredictor} для определения ритмичности колеса.
 * Делает ставки только если выявлен сильный паттерн смещения (доминирующий импульс).
 * Ставит на сектор (обычно 5 чисел), куда вероятнее всего прилетит шарик.
 */
@Strategy(tags = {Tag.PHYSICAL})
public class DealerSignatureStrategy extends AbstractStrategy {

    private final SectorMomentumPredictor predictor;
    private final double confidenceThreshold;

    public DealerSignatureStrategy(Config config) {
        super(config);
        this.predictor = new SectorMomentumPredictor();
        this.confidenceThreshold = 0.18; // Ставим, только если вероятность центра сектора высока
    }

    @Override
    public List<Bet> decide() {
        // Нужно минимум 10 спинов для калибровки
        if (getMarket().getHistory().size() < 10) {
            return List.of();
        }

        List<SpotPrediction> predictions = predictor.predictSpot(getState(), getConfig());

        if (predictions.isEmpty()) return List.of();

        // Проверяем "качество" сигнала. Если вероятности размазаны ровно - не ставим.
        // Ищем явный пик.
        double maxProb = predictions.stream()
                .mapToDouble(SpotPrediction::probability)
                .max()
                .orElse(0.0);

        if (maxProb < confidenceThreshold) {
            return List.of();
        }

        long baseBet = getMinBet();

        // Ставим на весь предсказанный сектор
        return predictions.stream()
                .map(p -> Bet.of(BetHelper.straightUp(p.spot()), baseBet))
                .toList();
    }
}