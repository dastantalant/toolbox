package com.trading.roulette.strategy.strategies.manager;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.strategy.predictor.EfficientCountPredictor;
import com.trading.roulette.strategy.predictor.ImprovedMarkovPredictor;
import com.trading.roulette.strategy.predictor.MultiOrderMarkovPredictor;
import com.trading.roulette.strategy.predictor.PatternPredictor;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.strategy.predictor.RandomPredictor;
import com.trading.roulette.api.sizing.DAlembertSizing;
import com.trading.roulette.api.sizing.FlatSizing;
import com.trading.roulette.api.sizing.KellySizing;
import com.trading.roulette.api.sizing.MartingaleSizing;
import com.trading.roulette.api.sizing.Sizing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Manager extends IStrategy {

    List<IStrategy> getPortfolio();

    IStrategy add(IStrategy strategy);

    List<IStrategy> add(List<IStrategy> strategies);

    IStrategy remove(IStrategy strategy);

    Optional<IStrategy> create(Class<? extends IStrategy> clazz, Config config);

    static List<IStrategy> generateAllCombinations(Config config) {
        List<IStrategy> combinations = new ArrayList<>();
        List<Predictor> predictors = new ArrayList<>();
        predictors.add(new MultiOrderMarkovPredictor());
        predictors.add(new ImprovedMarkovPredictor());
        predictors.add(new EfficientCountPredictor());
        predictors.add(new PatternPredictor());
        predictors.add(new RandomPredictor());

        List<Sizing> sizing = new ArrayList<>();
        sizing.add(new FlatSizing());
        sizing.add(new MartingaleSizing());
        sizing.add(new KellySizing());
        sizing.add(new DAlembertSizing());

        for (Predictor p : predictors) {
            for (Sizing s : sizing) {
                combinations.add(ModularStrategy.of(config, p, s));
            }
        }
        return combinations;
    }

    static String formatName(Predictor predictor, Sizing sizing) {
        String pName = predictor.getClass().getSimpleName().replace("Predictor", "");
        String sName = sizing.getClass().getSimpleName().replace("Sizing", "");
        return pName + "_" + sName;
    }
}
