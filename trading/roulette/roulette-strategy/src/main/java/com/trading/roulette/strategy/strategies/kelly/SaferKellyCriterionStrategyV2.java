package com.trading.roulette.strategy.strategies.kelly;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.State;
import com.trading.roulette.strategy.predictor.EfficientCountPredictor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Улучшенная SaferKellyCriterionStrategy.
 * <p>
 * Улучшения:
 * 1. <strong>Hard Cap (Жесткий потолок):</strong> Максимальная ставка ограничена 1.5% от банка,
 * независимо от того, насколько "уверен" алгоритм Келли.
 * 2. <strong>Fractional Kelly:</strong> Используется коэффициент 0.25 (четверть Келли) для снижения волатильности.
 * 3. <strong>Efficient Predictor:</strong> Использует более быстрый и стабильный предиктор.
 */
@Strategy(tags = {Tag.HYBRID, Tag.STATISTICAL})
public class SaferKellyCriterionStrategyV2 extends AbstractKellyCriterionStrategy {

    // Жесткое ограничение риска: не более 1.5% банка на спин
    private static final double MAX_BANKROLL_PERCENTAGE = 0.015;

    public SaferKellyCriterionStrategyV2(Config config) {
        super(config, KellyContext.builder()
                .kelly(0.25) // Используем "Четверть Келли" (очень консервативно)
                .maxBetPercentageCap(MAX_BANKROLL_PERCENTAGE)
                .minProbabilityThreshold(0.028) // Чуть выше 1/37
                .predictor(new EfficientCountPredictor())
                .build());
    }

    @Override
    public List<Bet> decide() {
        State state = getState();
        List<SpotPrediction> predictions = predictor.predictSpot(state, getConfig());

        Optional<KellyBet> optional = predictions.stream()
                .filter(item -> item.probability() > minProbabilityThreshold)
                .map(prediction -> calculateKellyFraction(prediction, kelly))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparingDouble(KellyBet::fraction));

        if (optional.isPresent()) {
            KellyBet kellyBet = optional.get();
            long balance = getVirtualBalance();

            // 1. Расчет по формуле Келли
            double fraction = kellyBet.fraction();

            // 2. Применение жесткого потолка (Safety Cap)
            double safeFraction = Math.min(fraction, MAX_BANKROLL_PERCENTAGE);

            long betValue = (long) (balance * safeFraction);
            betValue = Math.clamp(betValue, getMinBet(), getMaxBet());

            // 3. Доп. проверка: ставим только если есть деньги
            if (betValue <= balance && betValue >= getMinBet()) {
                return Bet.list(kellyBet.type(), betValue);
            }
        }
        return List.of();
    }
}

