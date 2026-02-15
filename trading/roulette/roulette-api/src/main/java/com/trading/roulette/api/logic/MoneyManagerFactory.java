package com.trading.roulette.api.logic;


import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.config.MoneyConfig;

public final class MoneyManagerFactory {
    private MoneyManagerFactory() {}

    public static MoneyManager create(Config config) {
        MoneyConfig mc = config.moneyConfig();
        if (mc == null) return new FlatMoneyManager(config);

        return switch (mc.type()) {
            case MARTINGALE -> new MartingaleMoneyManager(config, mc.multiplier());
            case DALEMBERT -> new DalembertMoneyManager(config, mc.step());
            case KELLY -> new KellyMoneyManager(config);
            default -> new FlatMoneyManager(config);
        };
    }
}