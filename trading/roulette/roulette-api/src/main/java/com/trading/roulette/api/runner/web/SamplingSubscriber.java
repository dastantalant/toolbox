package com.trading.roulette.api.runner.web;

import com.trading.roulette.api.Subscriber;
import com.trading.roulette.api.model.Snapshot;

import java.util.ArrayList;
import java.util.List;

public class SamplingSubscriber implements Subscriber {
    private final List<ChartPoint> chart = new ArrayList<>(5000);
    private final int sampleRate;
    private Snapshot lastSnapshot;

    public SamplingSubscriber(long totalRounds) {
        sampleRate = (int) Math.max(1, totalRounds / 1000);
    }

    @Override
    public void onRound(Snapshot snapshot) {
        lastSnapshot = snapshot;
        if (snapshot.roundNumber() % sampleRate == 0 || snapshot.roundNumber() == 1) {
            addPoint(snapshot);
        }
    }

    @Override
    public void onGameEnd(Snapshot snapshot) {
        this.lastSnapshot = snapshot;
        addPoint(snapshot);
    }

    private void addPoint(Snapshot snapshot) {
        // Формируем компактную строку ставок для экономии памяти/трафика
        // Вместо тяжелого JSON массива объектов, будет строка: "RED(10); ZERO(5)"
//        String betsInfo = "";
//        if (snapshot.lastBets() != null && !snapshot.lastBets().isEmpty()) {
//            betsInfo = snapshot.lastBets().stream()
//                    .map(b -> b.type().name() + "(" + b.value() + ")")
//                    .collect(Collectors.joining("; "));
//        }

        chart.add(new ChartPoint(
                snapshot.roundNumber(),
                snapshot.realBalance(),
                snapshot.lastProfit()
        ));
    }

    public List<ChartPoint> getChart() {
        return chart;
    }

    public WebStats getFinalStats() {
        if (lastSnapshot == null) return null;
        return new WebStats(
                lastSnapshot.roundNumber(),
                lastSnapshot.realBalance(),
                lastSnapshot.netProfit(),
                lastSnapshot.roiPercentage(),
                lastSnapshot.maxDrawdown(),
                lastSnapshot.winRatePercentage(),
                lastSnapshot.profitFactor(),
                lastSnapshot.totalWagered());
    }
}