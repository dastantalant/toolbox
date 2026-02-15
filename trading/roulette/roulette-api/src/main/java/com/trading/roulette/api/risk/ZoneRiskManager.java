package com.trading.roulette.api.risk;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.exception.StrategyException;
import com.trading.roulette.api.model.config.BetLimitConfig;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.model.config.RiskConfig;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.risk.limit.BalancePercentageLimitRule;
import com.trading.roulette.api.risk.limit.BetLimitRule;
import com.trading.roulette.api.risk.limit.FloatingMinBetRule;
import com.trading.roulette.api.risk.limit.PeakProtectLimitRule;
import com.trading.roulette.api.risk.limit.WinRateLimitRule;
import com.trading.roulette.api.utils.BetHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZoneRiskManager implements RiskManager {
    private final long profitZoneThreshold;
    private final long dangerZoneThreshold;
    private final double normalZoneBetCap;
    private final int penaltyDuration;
    private final long tableMinBet;
    private final long tableMaxBet;
    private final long tableLimit;
    private long minBet;
    private long maxBet;
    private boolean inPenaltyBox = false;
    private int penaltyRoundsRemaining = 0;
    private final List<BetLimitRule> limitRules;
    private final boolean strictMode;

    public ZoneRiskManager(Config config) {
        RiskConfig riskConfig = config.riskConfig();
        minBet = tableMinBet = config.minBet();
        maxBet = tableMaxBet = config.maxBet();
        tableLimit = config.tableLimit();
        dangerZoneThreshold = (long) (config.startBalance() * (riskConfig.dangerZoneThresholdRisk() / 100.0));
        profitZoneThreshold = (long) (config.startBalance() * (riskConfig.profitZoneThresholdRisk() / 100.0));
        normalZoneBetCap = riskConfig.normalZoneBetCapRisk() / 100.0;
        penaltyDuration = riskConfig.penaltyBoxDurationRisk();
        limitRules = Collections.unmodifiableList(createRules(config.betLimitConfig()));
        strictMode = config.riskConfig().strictMode();
    }

    private List<BetLimitRule> createRules(BetLimitConfig config) {
        var list = new ArrayList<BetLimitRule>();
        if (config == null) return list;
        if (config.maxBetBalancePercentageLimit() != null && config.maxBetBalancePercentageLimit() > 0)
            list.add(new BalancePercentageLimitRule(config.maxBetBalancePercentageLimit()));
        if (config.peakProtectDrawdownStepLimit() != null && config.peakProtectDrawdownStepLimit() > 0)
            list.add(new PeakProtectLimitRule(config.peakProtectDrawdownStepLimit(), config.peakProtectReductionFactorLimit() != null ? config.peakProtectReductionFactorLimit() : 50));
        if (config.minWinRateThresholdLimit() != null && config.minWinRateThresholdLimit() > 0)
            list.add(new WinRateLimitRule(config.minWinRateThresholdLimit(), config.minWinRateRoundsLimit() != null ? config.minWinRateRoundsLimit() : 50L));
        if (config.floatingMinBetPercentLimit() != null && config.floatingMinBetPercentLimit() > 0)
            list.add(new FloatingMinBetRule(config.floatingMinBetPercentLimit()));
        return list;
    }

    @Override
    public long calculateMin(IStrategy strategy) {
        if (inPenaltyBox) return 0;
        long dynamicMin = tableMinBet;
        for (BetLimitRule rule : limitRules) {
            dynamicMin = rule.calcMin(dynamicMin, maxBet, strategy);
        }
        minBet = dynamicMin;
        return minBet;
    }

    @Override
    public long calculateMax(IStrategy strategy) {
        if (inPenaltyBox) return 0;
        long dynamicMax = tableMaxBet;
        for (BetLimitRule rule : limitRules) {
            dynamicMax = rule.calcMax(minBet, dynamicMax, strategy);
        }
        if (minBet > dynamicMax) dynamicMax = minBet;
        long balance = strategy.getVirtualBalance();
        if (dynamicMax > balance) dynamicMax = balance;
        maxBet = dynamicMax;
        return maxBet;
    }

    @Override
    public long getMinBet() {
        return minBet;
    }

    @Override
    public long getMaxBet() {
        return maxBet;
    }

    @Override
    public List<Bet> apply(List<Bet> bets, IStrategy strategy) {
        long min = calculateMin(strategy);
        long max = calculateMax(strategy);
        if (min == 0 || bets.isEmpty()) return List.of();
        long currentBalance = strategy.getVirtualBalance();
        List<Bet> validatedBets = new ArrayList<>(bets.size());
        for (Bet bet : bets) {
            long val = bet.value();
            if (val <= 0) continue;
            if (val < min) {
                if (strictMode) throw new StrategyException("Bet under min limit: " + val + " < " + min);
                val = min;
            }
            if (val > max) {
                if (strictMode) throw new StrategyException("Bet over max limit: " + val + " > " + max);
                val = max;
            }
            if (val > currentBalance) {
                if (strictMode) throw new StrategyException("Bet exceeds balance: " + val + " > " + currentBalance);
                val = currentBalance;
            }
            if (val < 1) continue;
            validatedBets.add(Bet.of(bet.type(), val));
        }
        if (validatedBets.isEmpty()) return List.of();
        if (currentBalance > dangerZoneThreshold && currentBalance < profitZoneThreshold) {
            long totalValue = BetHelper.totalBetValue(validatedBets);
            long capLimit = (long) (currentBalance * normalZoneBetCap);
            if (totalValue > capLimit) validatedBets = scale(validatedBets, capLimit);
        }
        long totalValue = BetHelper.totalBetValue(validatedBets);
        if (totalValue > tableLimit) {
            if (strictMode) throw new StrategyException("Total bet exceeds table limit: " + totalValue);
            validatedBets = scale(validatedBets, tableLimit);
        }
        if (totalValue > currentBalance) validatedBets = scale(validatedBets, currentBalance);
        return validatedBets;
    }

    @Override
    public void updateState(long currentBalance) {
        if (inPenaltyBox) {
            penaltyRoundsRemaining--;
            if (penaltyRoundsRemaining <= 0) inPenaltyBox = false;
        } else if (currentBalance <= dangerZoneThreshold) {
            inPenaltyBox = true;
            penaltyRoundsRemaining = penaltyDuration;
        }
    }

    private List<Bet> scale(List<Bet> bets, long limit) {
        long totalValue = BetHelper.totalBetValue(bets);
        if (totalValue <= limit) return bets;
        double scalingFactor = (double) limit / totalValue;
        List<Bet> scaledBets = new ArrayList<>(bets.size());
        for (Bet bet : bets) {
            long newVal = (long) (bet.value() * scalingFactor);
            if (newVal >= 1) scaledBets.add(Bet.of(bet.type(), newVal));
        }
        return scaledBets;
    }
}