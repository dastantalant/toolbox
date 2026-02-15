package com.trading.roulette.strategy.strategies.special;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.strategy.predictor.EfficientCountPredictor;
import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;
import com.trading.roulette.strategy.predictor.PatternPredictor;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.strategy.predictor.SectorMomentumPredictor;
import com.trading.roulette.api.utils.BetHelper;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Байесовская Мета-Стратегия.
 * <p>
 * Вместо выбора одной тактики, она поддерживает "Вероятность истинности" (Belief) для нескольких гипотез.
 * Каждая гипотеза - это Предиктор (Марков, Счет, Паттерн).
 * После каждого раунда вероятности пересчитываются по формуле Байеса.
 * Ставка делается взвешенным мнением всех гипотез.
 */
@Strategy(tags = {Tag.AI, Tag.PREDICTIVE})
public class BayesianMetaStrategy extends AbstractStrategy {

    private final Map<Predictor, Double> beliefs; // P(Model)
    private final List<Predictor> models;
    private static final double DECAY = 0.98; // Забывание старых успехов

    public BayesianMetaStrategy(Config config) {
        super(config);
        models = List.of(new MultiOrderMarkovPredictor(),
                new EfficientCountPredictor(),
                new PatternPredictor(),
                new SectorMomentumPredictor());
        beliefs = new HashMap<>();
        double initialProb = 1.0 / models.size();
        models.forEach(m -> beliefs.put(m, initialProb));
    }

    @Override
    public List<Bet> decide() {
        Map<BetType, Double> aggregatedOpinion = new EnumMap<>(BetType.class);

        for (Predictor model : models) {
            double modelWeight = beliefs.get(model);

            // Получаем мнение модели (берем Straight Up для унификации)
            model.predictSpot(getState(), getConfig()).forEach(pred -> {
                BetType type = BetHelper.straightUp(pred.spot());
                double weightedProb = pred.probability() * modelWeight;
                aggregatedOpinion.merge(type, weightedProb, Double::sum);
            });
        }

        // Выбираем лучшие
        return aggregatedOpinion.entrySet().stream()
                .sorted(Map.Entry.<BetType, Double>comparingByValue().reversed())
                .limit(4)
                .filter(e -> e.getValue() > 0.15) // Порог уверенности
                .map(e -> Bet.of(e.getKey(), getMinBet()))
                .toList();
    }

    @Override
    protected void onRoundCustom(Spot spot, List<Bet> bets) {
        // Байесовское обновление (Bayesian Update)
        double normalizationFactor = 0.0;

        for (Predictor model : models) {
            double prior = beliefs.get(model);

            // Likelihood: Насколько хорошо модель предсказала этот исход?
            double likelihood = 0.001; // Базовая вероятность (чтобы не 0)

            var preds = model.predictSpot(getState(), getConfig());
            for (var p : preds) {
                if (p.spot() == spot) {
                    likelihood += p.probability();
                    break;
                }
            }

            double posterior = prior * likelihood;
            beliefs.put(model, posterior);
            normalizationFactor += posterior;
        }

        // Нормализация + Decay (чтобы система оставалась гибкой)
        for (Predictor model : models) {
            double norm = beliefs.get(model) / normalizationFactor;
            // Смешиваем с равномерным распределением (Re-injection of entropy)
            norm = (norm * DECAY) + ((1.0 - DECAY) / models.size());
            beliefs.put(model, norm);
        }
    }
}