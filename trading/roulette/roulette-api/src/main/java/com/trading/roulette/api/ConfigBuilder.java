package com.trading.roulette.api;

import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.model.config.BetLimitConfig;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.config.RawConfig;
import com.trading.roulette.api.model.config.RiskConfig;
import com.trading.roulette.api.model.config.StopConditionConfig;
import com.trading.roulette.api.model.config.TableRules;

import java.util.Deque;
import java.util.LinkedList;

public final class ConfigBuilder {
    private final Config.Builder config = Config.builder();
    private final StopConditionConfig.Builder stopConfig = StopConditionConfig.builder();
    private final Deque<Integer> initialHistory = new LinkedList<>();
    private final RiskConfig.Builder riskConfig = RiskConfig.builder();
    private final BetLimitConfig.Builder betLimitConfig = BetLimitConfig.builder();

    private Long startBalance;
    private Long minBet;
    private Long maxBet;
    private Long tableLimit;
    private RouletteType rouletteType;

    public ConfigBuilder name(String name) {
        config.name(name);
        return this;
    }

    public ConfigBuilder rouletteType(RouletteType type) {
        this.rouletteType = type;
        return this;
    }

    public ConfigBuilder debug(boolean debug) {
        config.debug(debug);
        return this;
    }

    public ConfigBuilder tableRules(TableRules tableRules) {
        config.tableRules(tableRules);
        return this;
    }

    public ConfigBuilder european() {
        return rouletteType(RouletteType.EUROPEAN);
    }

    public ConfigBuilder american() {
        return rouletteType(RouletteType.AMERICAN);
    }

    public ConfigBuilder realBalance(Integer realBalance) {
        return realBalance(realBalance.longValue());
    }

    public ConfigBuilder realBalance(Long realBalance) {
        if (realBalance == null || realBalance <= 0) throw new IllegalArgumentException("realBalance must be > 0");
        config.realBalance(realBalance);
        return this;
    }

    public ConfigBuilder startBalance(Integer startBalance) {
        return startBalance(startBalance.longValue());
    }

    public ConfigBuilder startBalance(Long startBalance) {
        if (startBalance == null || startBalance <= 0) throw new IllegalArgumentException("startBalance must be > 0");
        this.startBalance = startBalance;
        return this;
    }

    public ConfigBuilder minBet(Integer minBet) {
        return minBet(minBet.longValue());
    }

    public ConfigBuilder minBet(Long minBet) {
        this.minBet = minBet;
        return this;
    }

    public ConfigBuilder maxBet(Integer maxBet) {
        return maxBet(maxBet.longValue());
    }

    public ConfigBuilder maxBet(Long maxBet) {
        this.maxBet = maxBet;
        return this;
    }

    public ConfigBuilder tableLimit(Integer tableLimit) {
        return tableLimit(tableLimit.longValue());
    }

    public ConfigBuilder tableLimit(Long tableLimit) {
        this.tableLimit = tableLimit;
        return this;
    }

    public ConfigBuilder limits(Long minBet, Long maxBet, Long tableLimit) {
        if (minBet != null && minBet <= 0) throw new IllegalArgumentException("minBet <= 0");
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.tableLimit = tableLimit;
        return this;
    }

    // --- History & Configs Delegation ---
    public ConfigBuilder initialHistory(Integer history) {
        if (history == null) return this;
        initialHistory.addLast(history);
        return this;
    }

    public ConfigBuilder initialHistory(Deque<Integer> history) {
        if (history == null) return this;
        initialHistory.addAll(history);
        return this;
    }

    // --- Stop Config ---
    public ConfigBuilder stopLossPercentageStop(Integer val) {
        stopConfig.stopLossPercentageStop(val);
        return this;
    }

    public ConfigBuilder takeProfitPercentageStop(Integer val) {
        stopConfig.takeProfitPercentageStop(val);
        return this;
    }

    public ConfigBuilder trailingStopPercentageStop(Integer val) {
        stopConfig.trailingStopPercentageStop(val);
        return this;
    }

    public ConfigBuilder maxDrawdownPercentageStop(Integer val) {
        stopConfig.maxDrawdownPercentageStop(val);
        return this;
    }

    public ConfigBuilder maxWinStreakStop(Integer val) {
        stopConfig.maxWinStreakStop(val);
        return this;
    }

    public ConfigBuilder maxLossStreakStop(Integer val) {
        stopConfig.maxLossStreakStop(val);
        return this;
    }

    public ConfigBuilder maxRoundsStop(Long val) {
        stopConfig.maxRoundsStop(val);
        return this;
    }

    public ConfigBuilder stagnationWindowStop(Integer val) {
        stopConfig.stagnationWindowStop(val);
        return this;
    }

    public ConfigBuilder minWinRateThresholdStop(Integer val) {
        stopConfig.minWinRateThresholdStop(val);
        return this;
    }

    public ConfigBuilder minRoundsForWinRateStop(Long val) {
        stopConfig.minRoundsForWinRateStop(val);
        return this;
    }

    public ConfigBuilder equityCurveSmaPeriodStop(Integer val) {
        stopConfig.equityCurveSmaPeriodStop(val);
        return this;
    }

    // --- Risk Config ---
    public ConfigBuilder dangerZoneThresholdRisk(Integer val) {
        riskConfig.dangerZoneThresholdRisk(val);
        return this;
    }

    public ConfigBuilder profitZoneThresholdRisk(Integer val) {
        riskConfig.profitZoneThresholdRisk(val);
        return this;
    }

    public ConfigBuilder normalZoneBetCapRisk(Integer val) {
        riskConfig.normalZoneBetCapRisk(val);
        return this;
    }

    public ConfigBuilder penaltyBoxDurationRisk(Integer val) {
        riskConfig.penaltyBoxDurationRisk(val);
        return this;
    }

    public ConfigBuilder strictMode(boolean strictMode) {
        riskConfig.strictMode(strictMode);
        return this;
    }

    public ConfigBuilder riskConfig(RiskConfig riskConfig) {
        this.riskConfig.dangerZoneThresholdRisk(riskConfig.dangerZoneThresholdRisk())
                .profitZoneThresholdRisk(riskConfig.profitZoneThresholdRisk())
                .normalZoneBetCapRisk(riskConfig.normalZoneBetCapRisk())
                .penaltyBoxDurationRisk(riskConfig.penaltyBoxDurationRisk())
                .strictMode(riskConfig.strictMode());
        return this;
    }

    public ConfigBuilder riskDisabled() {
        return riskConfig(RiskConfig.disabled());
    }

    // --- Bet Limit Config ---
    public ConfigBuilder maxBetBalancePercentageLimit(Integer val) {
        betLimitConfig.maxBetBalancePercentageLimit(val);
        return this;
    }

    public ConfigBuilder peakProtectDrawdownStepLimit(Integer val) {
        betLimitConfig.peakProtectDrawdownStepLimit(val);
        return this;
    }

    public ConfigBuilder peakProtectReductionFactorLimit(Integer val) {
        betLimitConfig.peakProtectReductionFactorLimit(val);
        return this;
    }

    public ConfigBuilder minWinRateThresholdLimit(Integer val) {
        betLimitConfig.minWinRateThresholdLimit(val);
        return this;
    }

    public ConfigBuilder minWinRateRoundsLimit(Long val) {
        betLimitConfig.minWinRateRoundsLimit(val);
        return this;
    }

    public ConfigBuilder floatingMinBetPercentLimit(Integer val) {
        betLimitConfig.floatingMinBetPercentLimit(val);
        return this;
    }

    public ConfigBuilder betLimitConfig(BetLimitConfig betLimitConfig) {
        this.betLimitConfig.maxBetBalancePercentageLimit(betLimitConfig.maxBetBalancePercentageLimit());
        this.betLimitConfig.peakProtectDrawdownStepLimit(betLimitConfig.peakProtectDrawdownStepLimit());
        this.betLimitConfig.peakProtectReductionFactorLimit(betLimitConfig.peakProtectReductionFactorLimit());
        this.betLimitConfig.minWinRateThresholdLimit(betLimitConfig.minWinRateThresholdLimit());
        this.betLimitConfig.minWinRateRoundsLimit(betLimitConfig.minWinRateRoundsLimit());
        this.betLimitConfig.floatingMinBetPercentLimit(betLimitConfig.floatingMinBetPercentLimit());
        return this;
    }

    public Config build() {
        if (startBalance == null) {
            throw new IllegalStateException("Balance must be set before building config");
        }

        BetLimitConfig limitCfg = betLimitConfig.build();

        if (minBet == null) {
            Integer floatPercent = limitCfg.floatingMinBetPercentLimit();
            if (floatPercent != null && floatPercent > 0) {
                minBet = (startBalance * floatPercent) / 1000;
            } else {
                minBet = 1L;
            }
            if (minBet < 1) minBet = 1L;
        }

        if (maxBet == null) {
            Integer maxPercent = limitCfg.maxBetBalancePercentageLimit();
            if (maxPercent != null && maxPercent > 0) {
                maxBet = (startBalance * maxPercent) / 100;
            } else {
                maxBet = Math.min(startBalance, minBet * 500);
            }
            if (maxBet < minBet) maxBet = minBet;
        }

        if (tableLimit == null) {
            tableLimit = Math.max(maxBet * 100, startBalance * 10);
        }

        if (maxBet < minBet) maxBet = minBet;
        if (tableLimit < maxBet) tableLimit = maxBet;

        if (rouletteType == null) {
            rouletteType = RouletteType.AMERICAN;
        }

        config.startBalance(startBalance);
        config.minBet(minBet);
        config.maxBet(maxBet);
        config.tableLimit(tableLimit);
        config.rouletteType(rouletteType);

        return config.initial(initialHistory)
                .stopConfig(stopConfig.build())
                .riskConfig(riskConfig.build())
                .betLimitConfig(limitCfg)
                .build();
    }

    public static ConfigBuilder builder() {
        return new ConfigBuilder();
    }

    public static Config from(RawConfig rawConfig) {
        return rawConfig.toConfig();
    }
}