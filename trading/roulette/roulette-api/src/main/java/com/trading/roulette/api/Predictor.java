package com.trading.roulette.api;

import com.trading.roulette.api.model.BetTypePrediction;
import com.trading.roulette.api.model.ColorPrediction;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.State;
import com.trading.roulette.api.model.config.Config;

import java.util.List;

public interface Predictor {

    List<SpotPrediction> predictSpot(State state, Config config);

    default List<BetTypePrediction> predictType(State state, Config config) {
        return List.of();
    }

    default ColorPrediction predictColor(State state, Config config) {
        return ColorPrediction.of();
    }
}