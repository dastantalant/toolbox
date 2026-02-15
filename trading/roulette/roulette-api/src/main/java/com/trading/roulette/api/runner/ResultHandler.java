package com.trading.roulette.api.runner;

public interface ResultHandler extends AutoCloseable {
    void handle(SimulateResult result);
}