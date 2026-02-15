package com.trading.roulette.api.stop;

import com.trading.roulette.api.IStrategy;

@FunctionalInterface
public interface StopCondition  {
    boolean should(IStrategy strategy);
}