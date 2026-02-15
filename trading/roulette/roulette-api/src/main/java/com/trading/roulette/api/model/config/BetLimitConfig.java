package com.trading.roulette.api.model.config;

public record BetLimitConfig(
        Integer maxBetBalancePercentageLimit,
        Integer peakProtectDrawdownStepLimit,
        Integer peakProtectReductionFactorLimit,
        Integer minWinRateThresholdLimit,
        Long minWinRateRoundsLimit,
        Integer floatingMinBetPercentLimit
) {
    public BetLimitConfig(Integer maxBetBalancePercentageLimit, Integer peakProtectDrawdownStepLimit, Integer peakProtectReductionFactorLimit, Integer minWinRateThresholdLimit, Long minWinRateRoundsLimit, Integer floatingMinBetPercentLimit) {
        this.maxBetBalancePercentageLimit = clamp(maxBetBalancePercentageLimit);
        this.peakProtectDrawdownStepLimit = clamp(peakProtectDrawdownStepLimit);
        this.peakProtectReductionFactorLimit = clamp(peakProtectReductionFactorLimit);
        this.minWinRateThresholdLimit = clamp(minWinRateThresholdLimit);
        this.minWinRateRoundsLimit = minWinRateRoundsLimit == null ? 0 : minWinRateRoundsLimit;
        this.floatingMinBetPercentLimit = clamp(floatingMinBetPercentLimit, 1000);
    }

    /**
     * Возвращает пустую конфигурацию (все динамические ограничения отключены).
     * Используются только жесткие лимиты стола.
     */
    public static BetLimitConfig disabled() {
        return BetLimitConfig.builder().build();
    }

    /**
     * Возвращает сбалансированную конфигурацию.
     * <p>
     * - Макс ставка: 5% от баланса.
     * - Защита пика: При просадке 20% лимиты режутся на 25%.
     * - Защита винрейта: Если после 50 раундов винрейт < 30%, лимиты зажимаются.
     * </p>
     */
    public static BetLimitConfig balanced() {
        return BetLimitConfig.builder()
                .maxBetBalancePercentageLimit(5) // 0.05
                .peakProtectDrawdownStepLimit(20) // 0.20
                .peakProtectReductionFactorLimit(75) // 0.75
                .minWinRateThresholdLimit(30) // 0.30
                .minWinRateRoundsLimit(50L)
                .build();
    }

    /**
     * Возвращает защитную конфигурацию (Anti-Tilt).
     * <p>
     * Жесткое ограничение при просадках, чтобы предотвратить "тильт" и попытки отыграться ва-банк.
     * </p>
     */
    public static BetLimitConfig antiTilt() {
        return BetLimitConfig.builder()
                .maxBetBalancePercentageLimit(2) // Не больше 2% на ставку
                .peakProtectDrawdownStepLimit(10) // Каждые 10% просадки
                .peakProtectReductionFactorLimit(50) // Режем лимиты пополам
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static Integer clamp(Integer val) {
        if (val == null) return 0;
        return Math.min(val, 100);
    }

    private static Integer clamp(Integer val, Integer min) {
        if (val == null) return 0;
        return Math.min(val, min);
    }

    public static class Builder {
        private Integer maxBetBalancePercentageLimit;
        private Integer peakProtectDrawdownStepLimit;
        private Integer peakProtectReductionFactorLimit;
        private Integer minWinRateThresholdLimit;
        private Long minWinRateRoundsLimit;
        private Integer floatingMinBetPercentLimit;

        public BetLimitConfig build() {
            return new BetLimitConfig(
                    clamp(maxBetBalancePercentageLimit),
                    clamp(peakProtectDrawdownStepLimit),
                    clamp(peakProtectReductionFactorLimit),
                    clamp(minWinRateThresholdLimit),
                    minWinRateRoundsLimit,
                    clamp(floatingMinBetPercentLimit, 1000)
            );
        }

        public Builder maxBetBalancePercentageLimit(Integer maxBetBalancePercentageLimit) {
            this.maxBetBalancePercentageLimit = maxBetBalancePercentageLimit;
            return this;
        }

        public Builder peakProtectDrawdownStepLimit(Integer peakProtectDrawdownStepLimit) {
            this.peakProtectDrawdownStepLimit = peakProtectDrawdownStepLimit;
            return this;
        }

        public Builder peakProtectReductionFactorLimit(Integer peakProtectReductionFactorLimit) {
            this.peakProtectReductionFactorLimit = peakProtectReductionFactorLimit;
            return this;
        }

        public Builder minWinRateThresholdLimit(Integer minWinRateThresholdLimit) {
            this.minWinRateThresholdLimit = minWinRateThresholdLimit;
            return this;
        }

        public Builder minWinRateRoundsLimit(Long minWinRateRoundsLimit) {
            this.minWinRateRoundsLimit = minWinRateRoundsLimit;
            return this;
        }

        public Builder floatingMinBetPercentLimit(Integer floatingMinBetPercentLimit) {
            this.floatingMinBetPercentLimit = floatingMinBetPercentLimit;
            return this;
        }
    }
}
