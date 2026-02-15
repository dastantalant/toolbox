package com.trading.roulette.api.model.config;

import com.trading.roulette.api.ConfigBuilder;
import com.trading.roulette.api.enums.RouletteType;

import java.util.Deque;
import java.util.LinkedList;

public class RawConfig {
    private String name;
    private RouletteType type;
    private Long startBalance;
    private Long realBalance;
    private Long minBet;
    private Long maxBet;
    private Long tableLimit;

    private Integer stopLossPercentageStop;
    private Integer takeProfitPercentageStop;
    private Integer trailingStopPercentageStop;
    private Integer maxDrawdownPercentageStop;
    private Integer maxWinStreakStop;
    private Integer maxLossStreakStop;
    private Long maxRoundsStop;
    private Integer stagnationWindowStop;
    private Integer minWinRateThresholdStop;
    private Long minRoundsForWinRateStop;
    private Integer equityCurveSmaPeriodStop;

    private Deque<Integer> initialHistory;

    private Integer dangerZoneThresholdRisk;
    private Integer profitZoneThresholdRisk;
    private Integer normalZoneBetCapRisk;
    private Integer penaltyBoxDurationRisk;

    private Integer maxBetBalancePercentageLimit;
    private Integer peakProtectDrawdownStepLimit;
    private Integer peakProtectReductionFactorLimit;
    private Integer minWinRateThresholdLimit;
    private Long minWinRateRoundsLimit;
    private Integer floatingMinBetPercentLimit;

    public RawConfig() {initialHistory = new LinkedList<>();}

    public RawConfig(String name,
                     RouletteType type,
                     Long startBalance,
                     Long realBalance,
                     Long minBet,
                     Long maxBet,
                     Long tableLimit,
                     Integer stopLossPercentageStop,
                     Integer takeProfitPercentageStop,
                     Integer trailingStopPercentageStop,
                     Integer maxDrawdownPercentageStop,
                     Integer maxWinStreakStop,
                     Integer maxLossStreakStop,
                     Long maxRoundsStop,
                     Integer stagnationWindowStop,
                     Integer minWinRateThresholdStop,
                     Long minRoundsForWinRateStop,
                     Integer equityCurveSmaPeriodStop,
                     Deque<Integer> initialHistory,
                     Integer dangerZoneThresholdRisk,
                     Integer profitZoneThresholdRisk,
                     Integer normalZoneBetCapRisk,
                     Integer penaltyBoxDurationRisk,
                     Integer maxBetBalancePercentageLimit,
                     Integer peakProtectDrawdownStepLimit,
                     Integer peakProtectReductionFactorLimit,
                     Integer minWinRateThresholdLimit,
                     Long minWinRateRoundsLimit,
                     Integer floatingMinBetPercentLimit) {
        this.name = name;
        this.type = type;
        this.startBalance = startBalance;
        this.realBalance = realBalance;
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.tableLimit = tableLimit;
        this.stopLossPercentageStop = stopLossPercentageStop;
        this.takeProfitPercentageStop = takeProfitPercentageStop;
        this.trailingStopPercentageStop = trailingStopPercentageStop;
        this.maxDrawdownPercentageStop = maxDrawdownPercentageStop;
        this.maxWinStreakStop = maxWinStreakStop;
        this.maxLossStreakStop = maxLossStreakStop;
        this.maxRoundsStop = maxRoundsStop;
        this.stagnationWindowStop = stagnationWindowStop;
        this.minWinRateThresholdStop = minWinRateThresholdStop;
        this.minRoundsForWinRateStop = minRoundsForWinRateStop;
        this.equityCurveSmaPeriodStop = equityCurveSmaPeriodStop;
        this.initialHistory = initialHistory;
        this.dangerZoneThresholdRisk = dangerZoneThresholdRisk;
        this.profitZoneThresholdRisk = profitZoneThresholdRisk;
        this.normalZoneBetCapRisk = normalZoneBetCapRisk;
        this.penaltyBoxDurationRisk = penaltyBoxDurationRisk;
        this.maxBetBalancePercentageLimit = maxBetBalancePercentageLimit;
        this.peakProtectDrawdownStepLimit = peakProtectDrawdownStepLimit;
        this.peakProtectReductionFactorLimit = peakProtectReductionFactorLimit;
        this.minWinRateThresholdLimit = minWinRateThresholdLimit;
        this.minWinRateRoundsLimit = minWinRateRoundsLimit;
        this.floatingMinBetPercentLimit = floatingMinBetPercentLimit;
    }

    public Config toConfig() {return toConfig(this);}
    public static Config toConfig(RawConfig rawConfig) {
        return ConfigBuilder.builder()
                .name(rawConfig.name())
                .rouletteType(rawConfig.type())
                .startBalance(rawConfig.startBalance())
                .realBalance(rawConfig.realBalance())
                .limits(rawConfig.minBet(), rawConfig.maxBet(), rawConfig.tableLimit())

                .initialHistory(rawConfig.initialHistory())

                .stopLossPercentageStop(rawConfig.stopLossPercentageStop())
                .takeProfitPercentageStop(rawConfig.takeProfitPercentageStop())
                .trailingStopPercentageStop(rawConfig.trailingStopPercentageStop())
                .maxDrawdownPercentageStop(rawConfig.maxDrawdownPercentageStop())
                .maxWinStreakStop(rawConfig.maxWinStreakStop())
                .maxLossStreakStop(rawConfig.maxLossStreakStop())
                .maxRoundsStop(rawConfig.maxRoundsStop())
                .stagnationWindowStop(rawConfig.stagnationWindowStop())
                .minWinRateThresholdStop(rawConfig.minWinRateThresholdStop())
                .minRoundsForWinRateStop(rawConfig.minRoundsForWinRateStop())
                .equityCurveSmaPeriodStop(rawConfig.equityCurveSmaPeriodStop())

                .dangerZoneThresholdRisk(rawConfig.dangerZoneThresholdRisk())
                .profitZoneThresholdRisk(rawConfig.profitZoneThresholdRisk())
                .normalZoneBetCapRisk(rawConfig.normalZoneBetCapRisk())
                .penaltyBoxDurationRisk(rawConfig.penaltyBoxDurationRisk())

                .maxBetBalancePercentageLimit(rawConfig.maxBetBalancePercentageLimit())
                .peakProtectDrawdownStepLimit(rawConfig.peakProtectDrawdownStepLimit())
                .peakProtectReductionFactorLimit(rawConfig.peakProtectReductionFactorLimit())
                .minWinRateThresholdLimit(rawConfig.minWinRateThresholdLimit())
                .minWinRateRoundsLimit(rawConfig.minWinRateRoundsLimit())
                .floatingMinBetPercentLimit(rawConfig.floatingMinBetPercentLimit())

                .build();
    }

    public static RawConfig builder() {return new RawConfig();}
    public RawConfig build() {return this;}
    public String getName() {return name;}
    public String name() {return name;}
    public RawConfig name(String name) {this.name = name;return this;}
    public RouletteType getType() {return type;}
    public RouletteType type() {return type;}
    public RawConfig type(RouletteType type) {this.type = type;return this;}
    public Long getStartBalance() {return startBalance;}
    public Long startBalance() {return startBalance;}
    public RawConfig startBalance(Integer startBalance) {this.startBalance = startBalance.longValue();return this;}
    public RawConfig startBalance(Long startBalance) {this.startBalance = startBalance;return this;}
    public Long getRealBalance() {return realBalance;}
    public Long realBalance() {return realBalance;}
    public RawConfig realBalance(Integer realBalance) {this.realBalance = realBalance.longValue();return this;}
    public RawConfig realBalance(Long realBalance) {this.realBalance = realBalance;return this;}
    public Long getMinBet() {return minBet;}
    public Long minBet() {return minBet;}
    public RawConfig minBet(Integer minBet) {this.minBet = minBet.longValue();return this;}
    public RawConfig minBet(Long minBet) {this.minBet = minBet;return this;}
    public Long getMaxBet() {return maxBet;}
    public Long maxBet() {return maxBet;}
    public RawConfig maxBet(Integer maxBet) {this.maxBet = maxBet.longValue();return this;}
    public RawConfig maxBet(Long maxBet) {this.maxBet = maxBet;return this;}
    public Long getTableLimit() {return tableLimit;}
    public Long tableLimit() {return tableLimit;}
    public RawConfig tableLimit(Integer limit) {this.tableLimit = limit.longValue();return this;}
    public RawConfig tableLimit(Long limit) {this.tableLimit = limit;return this;}
    public Integer getStopLossPercentageStop() {return stopLossPercentageStop;}
    public Integer stopLossPercentageStop() {return stopLossPercentageStop;}
    public RawConfig stopLossPercentageStop(Integer stopLossPercentageStop) {this.stopLossPercentageStop = stopLossPercentageStop;return this;}
    public Integer getTakeProfitPercentageStop() {return takeProfitPercentageStop;}
    public Integer takeProfitPercentageStop() {return takeProfitPercentageStop;}
    public RawConfig takeProfitPercentageStop(Integer takeProfitPercentageStop) {this.takeProfitPercentageStop = takeProfitPercentageStop;return this;}
    public Integer getTrailingStopPercentageStop() {return trailingStopPercentageStop;}
    public Integer trailingStopPercentageStop() {return trailingStopPercentageStop;}
    public RawConfig trailingStopPercentageStop(Integer trailingStopPercentageStop) {this.trailingStopPercentageStop = trailingStopPercentageStop;return this;}
    public Integer getMaxDrawdownPercentageStop() {return maxDrawdownPercentageStop;}
    public Integer maxDrawdownPercentageStop() {return maxDrawdownPercentageStop;}
    public RawConfig maxDrawdownPercentageStop(Integer maxDrawdownPercentageStop) {this.maxDrawdownPercentageStop = maxDrawdownPercentageStop;return this;}
    public Integer getMaxWinStreakStop() {return maxWinStreakStop;}
    public Integer maxWinStreakStop() {return maxWinStreakStop;}
    public RawConfig maxWinStreakStop(Integer maxWinStreakStop) {this.maxWinStreakStop = maxWinStreakStop;return this;}
    public Integer getMaxLossStreakStop() {return maxLossStreakStop;}
    public Integer maxLossStreakStop() {return maxLossStreakStop;}
    public RawConfig maxLossStreakStop(Integer maxLossStreakStop) {this.maxLossStreakStop = maxLossStreakStop;return this;}
    public Long getMaxRoundsStop() {return maxRoundsStop;}
    public Long maxRoundsStop() {return maxRoundsStop;}
    public RawConfig maxRoundsStop(Long maxRoundsStop) {this.maxRoundsStop = maxRoundsStop;return this;}
    public Integer getStagnationWindowStop() {return stagnationWindowStop;}
    public Integer stagnationWindowStop() {return stagnationWindowStop;}
    public RawConfig stagnationWindowStop(Integer stagnationWindowStop) {this.stagnationWindowStop = stagnationWindowStop;return this;}
    public Integer getMinWinRateThresholdStop() {return minWinRateThresholdStop;}
    public Integer minWinRateThresholdStop() {return minWinRateThresholdStop;}
    public RawConfig minWinRateThresholdStop(Integer minWinRateThresholdStop) {this.minWinRateThresholdStop = minWinRateThresholdStop;return this;}
    public Long getMinRoundsForWinRateStop() {return minRoundsForWinRateStop;}
    public Long minRoundsForWinRateStop() {return minRoundsForWinRateStop;}
    public RawConfig minRoundsForWinRateStop(Long minRoundsForWinRateStop) {this.minRoundsForWinRateStop = minRoundsForWinRateStop;return this;}
    public Integer getEquityCurveSmaPeriodStop() {return equityCurveSmaPeriodStop;}
    public Integer equityCurveSmaPeriodStop() {return equityCurveSmaPeriodStop;}
    public RawConfig equityCurveSmaPeriodStop(Integer equityCurveSmaPeriodStop) {this.equityCurveSmaPeriodStop = equityCurveSmaPeriodStop;return this;}
    public Deque<Integer> getInitialHistory() {return initialHistory;}
    public Deque<Integer> initialHistory() {return initialHistory;}
    public RawConfig initialHistory(Deque<Integer> initial) {this.initialHistory = initial;return this;}
    public Integer getDangerZoneThresholdRisk() {return dangerZoneThresholdRisk;}
    public Integer dangerZoneThresholdRisk() {return dangerZoneThresholdRisk;}
    public RawConfig dangerZoneThresholdRisk(Integer dangerZoneThresholdRisk) {this.dangerZoneThresholdRisk = dangerZoneThresholdRisk;return this;}
    public Integer getProfitZoneThresholdRisk() {return profitZoneThresholdRisk;}
    public Integer profitZoneThresholdRisk() {return profitZoneThresholdRisk;}
    public RawConfig profitZoneThresholdRisk(Integer profitZoneThresholdRisk) {this.profitZoneThresholdRisk = profitZoneThresholdRisk;return this;}
    public Integer getNormalZoneBetCapRisk() {return normalZoneBetCapRisk;}
    public Integer normalZoneBetCapRisk() {return normalZoneBetCapRisk;}
    public RawConfig normalZoneBetCapRisk(Integer normalZoneBetCapRisk) {this.normalZoneBetCapRisk = normalZoneBetCapRisk;return this;}
    public Integer getPenaltyBoxDurationRisk() {return penaltyBoxDurationRisk;}
    public Integer penaltyBoxDurationRisk() {return penaltyBoxDurationRisk;}
    public RawConfig penaltyBoxDurationRisk(Integer penaltyBoxDurationRisk) {this.penaltyBoxDurationRisk = penaltyBoxDurationRisk;return this;}
    public Integer getMaxBetBalancePercentageLimit() {return maxBetBalancePercentageLimit;}
    public Integer maxBetBalancePercentageLimit() {return maxBetBalancePercentageLimit;}
    public RawConfig maxBetBalancePercentageLimit(Integer maxBetBalancePercentageLimit) {this.maxBetBalancePercentageLimit = maxBetBalancePercentageLimit;return this;}
    public Integer getPeakProtectDrawdownStepLimit() {return peakProtectDrawdownStepLimit;}
    public Integer peakProtectDrawdownStepLimit() {return peakProtectDrawdownStepLimit;}
    public RawConfig peakProtectDrawdownStepLimit(Integer peakProtectDrawdownStepLimit) {this.peakProtectDrawdownStepLimit = peakProtectDrawdownStepLimit;return this;}
    public Integer getPeakProtectReductionFactorLimit() {return peakProtectReductionFactorLimit;}
    public Integer peakProtectReductionFactorLimit() {return peakProtectReductionFactorLimit;}
    public RawConfig peakProtectReductionFactorLimit(Integer peakProtectReductionFactorLimit) {this.peakProtectReductionFactorLimit = peakProtectReductionFactorLimit;return this;}
    public Integer getMinWinRateThresholdLimit() {return minWinRateThresholdLimit;}
    public Integer minWinRateThresholdLimit() {return minWinRateThresholdLimit;}
    public RawConfig minWinRateThresholdLimit(Integer minWinRateThresholdLimit) {this.minWinRateThresholdLimit = minWinRateThresholdLimit;return this;}
    public Long getMinWinRateRoundsLimit() {return minWinRateRoundsLimit;}
    public Long minWinRateRoundsLimit() {return minWinRateRoundsLimit;}
    public RawConfig minWinRateRoundsLimit(Long minWinRateRoundsLimit) {this.minWinRateRoundsLimit = minWinRateRoundsLimit;return this;}
    public Integer getFloatingMinBetPercentLimit() {return floatingMinBetPercentLimit;}
    public Integer floatingMinBetPercentLimit() {return floatingMinBetPercentLimit;}
    public RawConfig floatingMinBetPercentLimit(Integer floatingMinBetPercentLimit) {this.floatingMinBetPercentLimit = floatingMinBetPercentLimit;return this;}
}