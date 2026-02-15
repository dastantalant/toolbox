package com.trading.roulette.api.optimization;


import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.SignalType;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.config.MoneyConfig;
import com.trading.roulette.api.model.config.StopConditionConfig;

import java.util.ArrayList;
import java.util.List;

public class StrategyOptimizer {

    private List<Config> generateConfigs() {
        List<Config> list = new ArrayList<>();

        // 1. Параметры для перебора (Grid Search Dimensions)
        // Варьируем Stop Loss: 10%, 20%, 30%, 50% от депозита
        int[] stopLossPercentages = {10, 20, 30, 50};

        // Варьируем Take Profit: 10%, 25%, 50%, 100% (удивительно, но иногда x2 баланса работает лучше мелких целей)
        int[] takeProfitPercentages = {10, 25, 50, 100};

        // Базовая ставка: 1, 5, 10 единиц
        long[] baseBets = {1L, 5L, 10L};

        // 2. Основной цикл генерации
        for (long baseBet : baseBets) {
            for (int stopLoss : stopLossPercentages) {
                for (int takeProfit : takeProfitPercentages) {

                    // A. Создаем конфигурацию условий остановки
                    StopConditionConfig stopConfig = StopConditionConfig.builder()
                            .stopLossPercentageStop(stopLoss)
                            .takeProfitPercentageStop(takeProfit)
                            // Можно добавить MaxDrawdown, если нужно жестче контролировать риски
                            // .maxDrawdownPercentageStop(stopLoss + 5)
                            .build();

                    // --- ВАРИАНТ 1: MARTINGALE (С разными множителями) ---
                    // Проверяем классический (2.0) и более агрессивный/консервативный
                    double[] multipliers = {1.5, 2.0, 2.5};
                    for (double mult : multipliers) {
                        MoneyConfig martiConfig = new MoneyConfig(
                                MoneyConfig.Type.MARTINGALE,
                                mult,
                                null // Step не нужен для мартингейла
                        );

                        list.add(buildConfig(baseBet, stopLoss, takeProfit, "MARTINGALE_x" + mult, stopConfig, martiConfig));
                    }

                    // --- ВАРИАНТ 2: D'ALEMBERT (С разными шагами) ---
                    // Шаг изменения ставки: 1x, 2x от базовой
                    long[] steps = {baseBet, baseBet * 2};
                    for (long step : steps) {
                        MoneyConfig dalembertConfig = new MoneyConfig(
                                MoneyConfig.Type.DALEMBERT,
                                null, // Multiplier не нужен
                                step
                        );

                        list.add(buildConfig(baseBet, stopLoss, takeProfit, "DALEMBERT_s" + step, stopConfig, dalembertConfig));
                    }

                    // --- ВАРИАНТ 3: FLAT (Фиксированная ставка) ---
                    // Для сравнения (Benchmark)
                    MoneyConfig flatConfig = MoneyConfig.flat();
                    list.add(buildConfig(baseBet, stopLoss, takeProfit, "FLAT", stopConfig, flatConfig));
                }
            }
        }

        return list;
    }

    // Вспомогательный метод для сборки Config, чтобы не дублировать код Builder-а
    private Config buildConfig(long baseBet, int sl, int tp, String variantName, StopConditionConfig stopCfg, MoneyConfig moneyCfg) {

        // Формируем уникальное имя для отчета: "Strat_Base10_SL20_TP50_MARTINGALE_x2.0"
        String configName = String.format("Base%d_SL%d_TP%d_%s", baseBet, sl, tp, variantName);

        return Config.builder()
                .name(configName)
                .startBalance(1000L) // Фиксированный стартовый баланс для всех тестов
                .minBet(baseBet)
                .maxBet(baseBet * 500) // Лимит стола (обычно 500-1000 ставок)
                .tableLimit(baseBet * 1000)

                // Внедряем изменяемые части
                .stopConfig(stopCfg)
                .moneyConfig(moneyCfg)

                // Фиксированные параметры
                .rouletteType(RouletteType.EUROPEAN) // Тестируем на Европейке (лучше RTP)
                .signalType(SignalType.RED) // Или SignalType.RANDOM, зависит от того, что оптимизируем
                .build();
    }
}