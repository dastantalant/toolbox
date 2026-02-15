package com.trading.roulette.strategy.strategies.balance;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Strategy(tags = {Tag.STATISTICAL})
public class DistantProgressionBalanceStrategy extends AbstractBalanceStrategy {

    private final List<BetType> targetBetTypes;

    public DistantProgressionBalanceStrategy(Config config) {
        super(config);
        targetBetTypes = List.of(BetType.FIRST_DOZEN, BetType.SECOND_DOZEN, BetType.THIRD_DOZEN);
    }

    @Override
    public List<Bet> decide() {
        return processBet(targetBetTypes.getFirst(), getState(), () -> {
            Map<BetType, Long> counters = new EnumMap<>(BetType.class);
            for (BetType type : targetBetTypes) {
                counters.put(type, 0L);
            }
            for (var spot : getState().getMarket().getHistory()) {
                for (BetType type : targetBetTypes) {
                    if (type.winning().contains(spot)) {
                        counters.put(type, counters.get(type) + 1);
                    }
                }
            }
            return counters.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(targetBetTypes.getFirst());
        });
    }

}