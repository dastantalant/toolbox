package com.trading.roulette.strategy.strategies.martingale;

import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

public abstract class AbstractMartingaleStrategy extends AbstractStrategy {
    private final List<BetType> types;
    private final int size;
    private final boolean single;
    protected final LongSupplier supplier;

    protected AbstractMartingaleStrategy(Config config, BetType type, LongSupplier supplier) {
        this(config, List.of(type), supplier);
    }

    protected AbstractMartingaleStrategy(Config config, List<BetType> types, LongSupplier supplier) {
        super(config);
        this.supplier = supplier;
        this.types = types;
        size = types.isEmpty() ? 1 : types.size();
        single = size == 1;
    }

    protected AbstractMartingaleStrategy(Config config) {
        this(config, List.of(BetType.RED));
    }

    protected AbstractMartingaleStrategy(Config config, LongSupplier supplier) {
        this(config, List.of(BetType.RED), supplier);
    }

    protected AbstractMartingaleStrategy(Config config, BetType type) {
        this(config, List.of(type));
    }

    protected AbstractMartingaleStrategy(Config config, List<BetType> types) {
        super(config);
        this.types = types;
        supplier = () -> calculateNext(config);
        size = types.isEmpty() ? 1 : types.size();
        single = size == 1;
    }

    @Override
    public List<Bet> decide() {
        if (single) {
            return single( getNextBetType());
        }
        return create();
    }

    public BetType getNextBetType() {
        return types.isEmpty() ? BetType.RED : types.getFirst();
    }

    protected long calculateNext(Config config) {
        return getLastBet() * 2;
    }


    protected List<Bet> create() {
        List<Bet> bets = new ArrayList<>(size);
        for (BetType type : types) {
            bets.add(create( type, size));
        }
        return bets;
    }

    protected List<Bet> single( BetType type) {
        return List.of(create( type,  1));
    }

    protected Bet create(BetType type, int divisor) {
        long balance = getVirtualBalance();
        long value = calculate();
        long safeValue = Math.min(value, balance / divisor);
        if (safeValue <= 0 && balance > 0) {
            safeValue = getMinBet();
        }
        return Bet.of(type, safeValue);
    }

    protected long calculate() {
        return (!isLastBet() || isLastBetWon()) ? getMinBet() : supplier.getAsLong();
    }
}
