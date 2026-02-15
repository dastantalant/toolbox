package com.trading.roulette.api.runner.supplier;

import com.trading.roulette.api.enums.RouletteType;
import com.trading.roulette.api.enums.Spot;

import java.util.LinkedList;
import java.util.Queue;

public class QueueSpotSupplier extends BaseSpotSupplier {
    private Queue<Integer> data;
    private final Queue<Integer> original;

    public QueueSpotSupplier(RouletteType rouletteType, Queue<Integer> data) {
        super(rouletteType, data.size());
        this.data = new LinkedList<>(data);
        original = new LinkedList<>(data);
    }

    public static SpotSupplier of(RouletteType rouletteType, Queue<Integer> data) {
        return new QueueSpotSupplier(rouletteType, data);
    }

    @Override
    protected Spot getInternal() {
        if (data.isEmpty()) {
            return null;
        }
        return Spot.getByNumber(data.poll());
    }

    @Override
    public void reset() {
        data.clear();
        data = new LinkedList<>(original);
    }
}
