package com.trading.roulette.api;

import com.trading.roulette.api.model.Snapshot;

public interface Publisher {

    Subscriber addSubscriber(Subscriber subscriber);

    Snapshot publish(Snapshot snapshot);

}