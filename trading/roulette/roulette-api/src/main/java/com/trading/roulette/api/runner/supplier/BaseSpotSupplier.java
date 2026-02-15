package com.trading.roulette.api.runner.supplier;

import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.Spot;

import java.util.function.Consumer;

public abstract class BaseSpotSupplier implements SpotSupplier {
    private final RouletteType rouletteType;
    private final long max;
    private long counter;

    protected BaseSpotSupplier(RouletteType rouletteType, long max) {
        if (max <= 0) {
            throw new IllegalArgumentException("max must be > 0");
        }
        this.rouletteType = rouletteType;
        this.max = max;
    }

    @Override
    public Spot get() {
        if (!hasNext()) {
            throw new IllegalStateException("The generation limit has been reached: " + getCount() + " / " + getMax());
        }
        incrementCounter();
        return getInternal();
    }

    protected abstract Spot getInternal();

    @Override
    public boolean apply(Consumer<Spot> consumer) {
        if (!hasNext()) {
            return false;
        }
        Spot spot = get();
        consumer.accept(spot);
        return true;
    }

    public boolean hasNext() {
        return counter < max;
    }

    public long getRemaining() {
        return Math.max(0, max - counter);
    }

    public long getCount() {
        return counter;
    }

    public void setCount(long count) {
        this.counter = count;
    }

    protected void incrementCounter() {
        counter++;
    }

    public void reset() {
        counter = 0;
    }

    public final RouletteType getRouletteType() {
        return rouletteType;
    }

    public final long getMax() {
        return max;
    }

    public final long getCounter() {
        return counter;
    }
}
