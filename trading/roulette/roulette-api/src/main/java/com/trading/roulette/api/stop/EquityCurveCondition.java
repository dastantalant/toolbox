package com.trading.roulette.api.stop;

import com.trading.roulette.api.IStrategy;
import java.util.ArrayDeque;
import java.util.Deque;

public class EquityCurveCondition implements StopCondition {
    private final int smaPeriod;
    private final Deque<Long> balances;

    public EquityCurveCondition(int smaPeriod) {
        this.smaPeriod = smaPeriod;
        this.balances = new ArrayDeque<>(smaPeriod + 1);
    }

    @Override
    public boolean should(IStrategy strategy) {
        balances.offer(strategy.getVirtualBalance());
        if (balances.size() > smaPeriod) {
            balances.poll();

            double sum = 0;
            for (Long val : balances) {
                sum += val;
            }
            double avg = sum / balances.size();
            return strategy.getVirtualBalance() < avg * 0.95;
        }
        return false;
    }
}