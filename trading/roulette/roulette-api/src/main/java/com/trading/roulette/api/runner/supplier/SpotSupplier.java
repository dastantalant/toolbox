package com.trading.roulette.api.runner.supplier;

import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.Spot;

import java.util.function.Consumer;

public interface SpotSupplier {

    Spot get();

    RouletteType getRouletteType();

    boolean hasNext();

    boolean apply(Consumer<Spot> consumer);

    long getRemaining();

    long getCount();

    long getMax();

    void reset();
}
