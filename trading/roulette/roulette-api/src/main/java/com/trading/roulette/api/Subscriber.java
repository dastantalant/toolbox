package com.trading.roulette.api;

import com.trading.roulette.api.model.Snapshot;

import java.util.Deque;
import java.util.LinkedList;

public interface Subscriber {

    void onRound(Snapshot snapshot);

    default void onGameEnd(Snapshot snapshot) {
    }

    default Deque<Snapshot> deque() {
        return new LinkedList<>();
    }

    static Subscriber storage() {
        return new Storage(new LinkedList<>());
    }

    record Storage(Deque<Snapshot> deque) implements Subscriber {
        public void onRound(Snapshot snapshot) {
            deque.addLast(snapshot);
        }
    }
}