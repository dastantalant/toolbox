package com.trading.roulette.api.exception;

public class StrategyException extends RuntimeException {
    public StrategyException() {
    }

    public StrategyException(String message) {
        super(message);
    }
    public StrategyException(String message, Throwable cause) {
        super(message, cause);
    }
}
