package com.trading.roulette.api.enums;

import java.security.SecureRandom;
import java.util.Random;
import java.util.random.RandomGenerator;

public final class Constants {
    private Constants() {
    }

    public static final Random RANDOM = new SecureRandom();

    public static final int SPOT_HISTORY_LIST_SIZE = 10000;

    public static final int BALANCE_HISTORY_SIZE = 10000;

    public static final double KELLY_FRACTION_DEFAULT = 0.25;
}