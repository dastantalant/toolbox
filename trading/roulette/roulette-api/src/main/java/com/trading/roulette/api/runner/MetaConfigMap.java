package com.trading.roulette.api.runner;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.model.config.Config;

public record MetaConfigMap(Config config, Class<? extends IStrategy> clazz) {
}
