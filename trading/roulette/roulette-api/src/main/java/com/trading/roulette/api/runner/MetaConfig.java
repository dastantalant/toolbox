package com.trading.roulette.api.runner;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.runner.supplier.SpotSupplier;

public record MetaConfig(Config config, Class<? extends IStrategy> clazz, SpotSupplier supplier) {
}
