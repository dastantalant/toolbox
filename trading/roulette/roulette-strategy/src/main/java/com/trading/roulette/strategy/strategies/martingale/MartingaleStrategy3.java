package com.trading.roulette.strategy.strategies.martingale;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.Predictor;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.ColorPrediction;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.strategy.predictor.CountPredictor2;

import java.util.List;

@Strategy(tags = {Tag.PROGRESSIVE})
public class MartingaleStrategy3 extends AbstractMartingaleStrategy {

    private final Predictor predictor;

    public MartingaleStrategy3(Config config) {
        super(config, List.of());
        predictor = new CountPredictor2();
    }

    @Override
    public BetType getNextBetType() {
        ColorPrediction prediction = predictor.predictColor(getState(), getConfig());
        return prediction.black() <= prediction.red() ? BetType.RED : BetType.BLACK;
    }
}