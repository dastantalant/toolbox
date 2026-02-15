package com.trading.roulette.api.model.config;

import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.SignalType;

import java.util.ArrayDeque;
import java.util.Deque;

public record Config(
        String name,
        RouletteType rouletteType,
        boolean debug,
        TableRules tableRules,
        Long minBet,
        Long maxBet,
        Long startBalance,
        Long realBalance,
        Long tableLimit,
        StopConditionConfig stopConfig,
        Deque<Integer> initial,
        RiskConfig riskConfig,
        BetLimitConfig betLimitConfig,
        MoneyConfig moneyConfig,
        SignalType signalType
) {
    public Deque<Integer> initial() {
        return initial == null ? new ArrayDeque<>() : initial;
    }

    public RiskConfig riskConfig() {
        return riskConfig == null ? RiskConfig.defaultConfig() : riskConfig;
    }
    public SignalType signalType() {
        return signalType == null ? SignalType.NONE : signalType;
    }
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private RouletteType rouletteType;
        private boolean debug = false;
        private TableRules tableRules = TableRules.standard();
        private Long minBet;
        private Long maxBet;
        private Long startBalance;
        private Long realBalance;
        private Long tableLimit;
        private StopConditionConfig stopConfig;
        private Deque<Integer> initial;
        private RiskConfig riskConfig;
        private BetLimitConfig betLimitConfig;
        private MoneyConfig moneyConfig;
        private SignalType signalType;

        public Config build() {
            realBalance = realBalance == null ? startBalance : realBalance;
            return new Config(name, rouletteType, debug, tableRules, minBet, maxBet, startBalance, realBalance, tableLimit,
                    stopConfig, initial, riskConfig, betLimitConfig, moneyConfig, signalType);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder rouletteType(RouletteType rouletteType) {
            this.rouletteType = rouletteType;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder tableRules(TableRules tableRules) {
            this.tableRules = tableRules;
            return this;
        }

        public Builder minBet(Long minBet) {
            this.minBet = minBet;
            return this;
        }

        public Builder maxBet(Long maxBet) {
            this.maxBet = maxBet;
            return this;
        }

        public Builder startBalance(Long startBalance) {
            this.startBalance = startBalance;
            return this;
        }

        public Builder realBalance(Long realBalance) {
            this.realBalance = realBalance;
            return this;
        }

        public Builder tableLimit(Long tableLimit) {
            this.tableLimit = tableLimit;
            return this;
        }

        public Builder stopConfig(StopConditionConfig stopConfig) {
            this.stopConfig = stopConfig;
            return this;
        }

        public Builder initial(Deque<Integer> initial) {
            this.initial = initial;
            return this;
        }

        public Builder riskConfig(RiskConfig riskConfig) {
            this.riskConfig = riskConfig;
            return this;
        }

        public Builder betLimitConfig(BetLimitConfig betLimitConfig) {
            this.betLimitConfig = betLimitConfig;
            return this;
        }

        public Builder moneyConfig(MoneyConfig moneyConfig) {
            this.moneyConfig = moneyConfig;
            return this;
        }

        public Builder signalType(SignalType signalType) {
            this.signalType = signalType;
            return this;
        }

    }
}