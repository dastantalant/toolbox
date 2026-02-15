package com.trading.roulette.api.model;

import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.utils.BetHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.trading.roulette.api.enums.Constants.BALANCE_HISTORY_SIZE;

public final class Metric {
    private final String strategyName;
    private final long startBalance;

    private final long[] historyBuffer;
    private int historyIndex = 0;
    private int historyCount = 0;

    private long wonCount;
    private long lostCount;
    private long betCount;
    private long roundNumber;

    private long peakBalance;
    private long totalWagered;
    private long totalPayout;

    private double maxDrawdown;
    private double m2 = 0.0;
    private double meanReturn = 0.0;

    private int currentDrawdownDuration = 0;
    private int maxDrawdownDuration = 0;
    private long peakRound = 0;

    public Metric(String strategyName, long startBalance) {
        this.strategyName = strategyName;
        this.startBalance = startBalance;
        peakBalance = startBalance;
        historyBuffer = new long[BALANCE_HISTORY_SIZE];
    }

    public static Metric from(String strategyName, long startBalance) {
        return new Metric(strategyName, startBalance);
    }

    public void update(long balance, long totalBetValue, long totalPayout) {
        roundNumber++;
        betCount++;
        totalWagered += totalBetValue;
        this.totalPayout += totalPayout;

        if (totalPayout > totalBetValue) wonCount++;
        else lostCount++;

        long profit = totalPayout - totalBetValue;
        double delta = profit - meanReturn;
        meanReturn += delta / roundNumber;
        double delta2 = profit - meanReturn;
        m2 += delta * delta2;

        if (balance >= peakBalance) {
            peakBalance = balance;
            peakRound = roundNumber;
            currentDrawdownDuration = 0;
        } else {
            double currentDrawdown = (double) (peakBalance - balance) / peakBalance;
            if (currentDrawdown > maxDrawdown) {
                maxDrawdown = currentDrawdown;
            }
            currentDrawdownDuration = (int) (roundNumber - peakRound);
            if (currentDrawdownDuration > maxDrawdownDuration) {
                maxDrawdownDuration = currentDrawdownDuration;
            }
        }
        addHistory(balance);
    }

    public void updateEmpty(long balance) {
        roundNumber++;
        addHistory(balance);
    }

    private void addHistory(long balance) {
        historyBuffer[historyIndex] = balance;
        historyIndex = (historyIndex + 1) % BALANCE_HISTORY_SIZE;
        if (historyCount < BALANCE_HISTORY_SIZE) {
            historyCount++;
        }
    }

    public Snapshot snapshot(boolean isLive, List<Bet> bets, Spot spot, long balance, long oldBalance) {
        return snapshot(isLive, bets, spot, balance, oldBalance, true);
    }

    public Snapshot snapshot(boolean isLive, List<Bet> bets, Spot spot, long balance, long oldBalance, boolean detailed) {
        if (!detailed) return null;

        long netProfit = balance - startBalance;
        double drawdown = ((peakBalance > 0) ? (double) (peakBalance - balance) / peakBalance : 0) * 100;

        double roiPercentage = (startBalance == 0) ? 0.0 : ((double) netProfit / startBalance) * 100.0;
        double winRatePercentage = (betCount == 0) ? 0.0 : ((double) wonCount / betCount) * 100.0;
        double profitFactor = (totalWagered == 0) ? 0.0 : (double) totalPayout / totalWagered;

        double variance = (roundNumber > 1) ? m2 / (roundNumber - 1) : 0.0;
        double stdev = Math.sqrt(variance);
        double sharpeRatio = (stdev != 0) ? meanReturn / stdev : 0.0;

        return Snapshot.builder()
                .strategyName(strategyName)
                .roundNumber(roundNumber)
                .virtualBalance(balance)
                .startBalance(startBalance)
                .lastProfit(balance - oldBalance)
                .lastBets(bets != null ? List.copyOf(bets) : List.of())
                .roundBetAmount(BetHelper.totalBetValue(bets))
                .netProfit(netProfit)
                .roiPercentage(roiPercentage)
                .winRatePercentage(winRatePercentage)
                .lastSpot(spot)
                .betCount(betCount)
                .totalWon(wonCount)
                .totalLost(lostCount)
                .peakBalance(peakBalance)
                .totalWagered(totalWagered)
                .totalPayout(totalPayout)
                .currentDrawdown(drawdown)
                .maxDrawdown(maxDrawdown * 100)
                .profitFactor(profitFactor)
                .isLive(isLive)
                .sharpeRatio(sharpeRatio)
                .maxDrawdownDuration(maxDrawdownDuration)
                .build();
    }

    public List<Long> getHistoryBalance() {
        List<Long> result = new ArrayList<>(historyCount);
        if (historyCount < BALANCE_HISTORY_SIZE) {
            for (int i = 0; i < historyCount; i++) {
                result.add(historyBuffer[i]);
            }
        } else {
            int current = historyIndex;
            for (int i = 0; i < BALANCE_HISTORY_SIZE; i++) {
                result.add(historyBuffer[current]);
                current = (current + 1) % BALANCE_HISTORY_SIZE;
            }
        }
        return Collections.unmodifiableList(result);
    }

    public void resetCount() {
        betCount = 0;
        wonCount = 0;
        lostCount = 0;
        roundNumber = 0;
        historyIndex = 0;
        historyCount = 0;
        m2 = 0.0;
        meanReturn = 0.0;
        peakBalance = startBalance;
        maxDrawdown = 0;
        totalWagered = 0;
        totalPayout = 0;
        maxDrawdownDuration = 0;
        currentDrawdownDuration = 0;
        peakRound = 0;
    }

    public double getAverage() {
        return betCount == 0 ? 0 : (double) wonCount / betCount;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public long getStartBalance() {
        return startBalance;
    }

    public long getWonCount() {
        return wonCount;
    }

    public long getLostCount() {
        return lostCount;
    }

    public long getBetCount() {
        return betCount;
    }

    public long getRoundNumber() {
        return roundNumber;
    }

    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public long getPeakBalance() {
        return peakBalance;
    }

    public long getTotalWagered() {
        return totalWagered;
    }

    public long getTotalPayout() {
        return totalPayout;
    }
}