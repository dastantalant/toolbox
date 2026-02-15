package com.trading.roulette.api.runner;

public record SimulateResult(
        String config,
        String strategy,
        RunnerResult runnerResult
) {
    public static SimulateResult of(String config, String strategy, RunnerResult runnerResult) {
        return new SimulateResult(config, strategy, runnerResult);
    }
}
