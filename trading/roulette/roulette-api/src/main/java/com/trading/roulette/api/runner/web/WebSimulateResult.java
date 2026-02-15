package com.trading.roulette.api.runner.web;

import java.util.List;

public record WebSimulateResult(
        String strategyName,
        String configName,
        WebStats stats,
        List<ChartPoint> chart
) {
    public static  WebSimulateResult of(String strategyName, String configName, WebStats stats, List<ChartPoint> chart) {
        return new WebSimulateResult(strategyName, configName, stats, chart);
    }
}


