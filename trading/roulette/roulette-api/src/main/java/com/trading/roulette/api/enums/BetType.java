package com.trading.roulette.api.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.trading.roulette.api.enums.RouletteType.AMERICAN;
import static com.trading.roulette.api.enums.RouletteType.EUROPEAN;

public enum BetType {
    SPOT_01(35, Spot.SPOT_01),
    SPOT_02(35, Spot.SPOT_02),
    SPOT_03(35, Spot.SPOT_03),
    SPOT_04(35, Spot.SPOT_04),
    SPOT_05(35, Spot.SPOT_05),
    SPOT_06(35, Spot.SPOT_06),
    SPOT_07(35, Spot.SPOT_07),
    SPOT_08(35, Spot.SPOT_08),
    SPOT_09(35, Spot.SPOT_09),
    SPOT_10(35, Spot.SPOT_10),
    SPOT_11(35, Spot.SPOT_11),
    SPOT_12(35, Spot.SPOT_12),
    SPOT_13(35, Spot.SPOT_13),
    SPOT_14(35, Spot.SPOT_14),
    SPOT_15(35, Spot.SPOT_15),
    SPOT_16(35, Spot.SPOT_16),
    SPOT_17(35, Spot.SPOT_17),
    SPOT_18(35, Spot.SPOT_18),
    SPOT_19(35, Spot.SPOT_19),
    SPOT_20(35, Spot.SPOT_20),
    SPOT_21(35, Spot.SPOT_21),
    SPOT_22(35, Spot.SPOT_22),
    SPOT_23(35, Spot.SPOT_23),
    SPOT_24(35, Spot.SPOT_24),
    SPOT_25(35, Spot.SPOT_25),
    SPOT_26(35, Spot.SPOT_26),
    SPOT_27(35, Spot.SPOT_27),
    SPOT_28(35, Spot.SPOT_28),
    SPOT_29(35, Spot.SPOT_29),
    SPOT_30(35, Spot.SPOT_30),
    SPOT_31(35, Spot.SPOT_31),
    SPOT_32(35, Spot.SPOT_32),
    SPOT_33(35, Spot.SPOT_33),
    SPOT_34(35, Spot.SPOT_34),
    SPOT_35(35, Spot.SPOT_35),
    SPOT_36(35, Spot.SPOT_36),
    ZERO(35, EnumSet.of(EUROPEAN, AMERICAN), Spot.SPOT_0),
    DOUBLE_ZERO(35, EnumSet.of(AMERICAN), Spot.SPOT_00),

    SPLIT_1_2(17, Spot.SPOT_01, Spot.SPOT_02),
    SPLIT_2_3(17, Spot.SPOT_02, Spot.SPOT_03),
    SPLIT_35_36(17, Spot.SPOT_35, Spot.SPOT_36),
    SPLIT_1_4(17, Spot.SPOT_01, Spot.SPOT_04),
    SPLIT_8_11(17, Spot.SPOT_08, Spot.SPOT_11),
    SPLIT_10_13(17, Spot.SPOT_10, Spot.SPOT_13),
    SPLIT_17_20(17, Spot.SPOT_17, Spot.SPOT_20),
    SPLIT_26_29(17, Spot.SPOT_26, Spot.SPOT_29),
    SPLIT_28_31(17, Spot.SPOT_28, Spot.SPOT_31),

    STREET_1_2_3(11, Spot.SPOT_01, Spot.SPOT_02, Spot.SPOT_03),
    STREET_34_35_36(11, Spot.SPOT_34, Spot.SPOT_35, Spot.SPOT_36),

    CORNER_1_2_4_5(8, Spot.SPOT_01, Spot.SPOT_02, Spot.SPOT_04, Spot.SPOT_05),
    CORNER_32_33_35_36(8, Spot.SPOT_32, Spot.SPOT_33, Spot.SPOT_35, Spot.SPOT_36),

    SIX_LINE_1_2_3_4_5_6(5, Spot.SPOT_01, Spot.SPOT_02, Spot.SPOT_03, Spot.SPOT_04, Spot.SPOT_05, Spot.SPOT_06),
    SIX_LINE_31_32_33_34_35_36(5, Spot.SPOT_31, Spot.SPOT_32, Spot.SPOT_33, Spot.SPOT_34, Spot.SPOT_35, Spot.SPOT_36),

    TOP_LINE_OR_BASKET(6, EnumSet.of(AMERICAN), Spot.SPOT_0, Spot.SPOT_00, Spot.SPOT_01, Spot.SPOT_02, Spot.SPOT_03),
    TRIO_0_1_2(11, EnumSet.of(EUROPEAN), Spot.SPOT_0, Spot.SPOT_01, Spot.SPOT_02),
    TRIO_0_2_3(11, EnumSet.of(EUROPEAN), Spot.SPOT_0, Spot.SPOT_02, Spot.SPOT_03),
    SPLIT_0_00(17, EnumSet.of(AMERICAN), Spot.SPOT_0, Spot.SPOT_00),
    SPLIT_0_2(17, EnumSet.of(EUROPEAN), Spot.SPOT_0, Spot.SPOT_02),
    STREET_0_00_2(11, EnumSet.of(AMERICAN), Spot.SPOT_0, Spot.SPOT_00, Spot.SPOT_02),
    CORNER_0_1_2_3(8, EnumSet.of(EUROPEAN), Spot.SPOT_0, Spot.SPOT_01, Spot.SPOT_02, Spot.SPOT_03),

    FIRST_COLUMN(2, Spot.FIRST_COLUMN_SPOTS),
    SECOND_COLUMN(2, Spot.SECOND_COLUMN_SPOTS),
    THIRD_COLUMN(2, Spot.THIRD_COLUMN_SPOTS),
    FIRST_DOZEN(2, Spot.FIRST_DOZEN_SPOTS),
    SECOND_DOZEN(2, Spot.SECOND_DOZEN_SPOTS),
    THIRD_DOZEN(2, Spot.THIRD_DOZEN_SPOTS),
    ONE_TO_EIGHTEEN(1, Spot.LOW_SPOTS),
    EVEN(1, Spot.EVENS),
    RED(1, Spot.REDS),
    BLACK(1, Spot.BLACKS),
    ODD(1, Spot.ODDS),
    NINETEEN_TO_THIRTY_SIX(1, Spot.HIGH_SPOTS);

    private final int payout;
    private final Set<RouletteType> availableIn;
    private final Set<Spot> winning;

    private static final Map<Spot, BetType> SPOT_MAP = new EnumMap<>(Spot.class);
    private static final Map<RouletteType, List<BetType>> AVAILABLE_BETS = new ConcurrentHashMap<>();

    static {
        SPOT_MAP.put(Spot.SPOT_0, ZERO);
        SPOT_MAP.put(Spot.SPOT_00, DOUBLE_ZERO);
        for (Spot spot : Spot.values()) {
            if (spot.number() > 0) {
                try {
                    SPOT_MAP.put(spot, BetType.valueOf(spot.name()));
                } catch (IllegalArgumentException e) {
                }
            }
        }
    }

    BetType(int payout, Spot... spots) {
        this(payout, EnumSet.allOf(RouletteType.class), EnumSet.copyOf(List.of(spots)));
    }

    BetType(int payout, EnumSet<RouletteType> availableIn, Spot... spots) {
        this(payout, availableIn, EnumSet.copyOf(List.of(spots)));
    }

    BetType(int payout, Set<Spot> spots) {
        this(payout, EnumSet.allOf(RouletteType.class), spots);
    }

    BetType(int payout, EnumSet<RouletteType> availableIn, Set<Spot> spots) {
        this.payout = payout;
        this.availableIn = availableIn;
        winning = (spots == null || spots.isEmpty()) ? EnumSet.noneOf(Spot.class) : spots;
    }

    public static List<BetType> allCovering(Spot spot) {
        return Stream.of(BetType.values())
                .filter(type -> type.winning().contains(spot))
                .toList();
    }

    public static List<BetType> availableList(RouletteType rouletteType) {
        Objects.requireNonNull(rouletteType, "rouletteType не может быть null");
        return AVAILABLE_BETS.computeIfAbsent(rouletteType, key -> {
            List<BetType> result = new ArrayList<>();
            for (BetType bet : values())
                if (bet.isAvailableIn(key)) result.add(bet);
            return Collections.unmodifiableList(result);
        });
    }

    public boolean isAvailableIn(RouletteType rouletteType) {
        return availableIn.contains(rouletteType);
    }

    public int returnMultiplier() {
        return payout + 1;
    }

    public static BetType straightUpBetFor(Spot spot) {
        BetType bet = SPOT_MAP.get(spot);
        if (bet == null) {
            throw new IllegalArgumentException("Нет ставки 'Straight Up' для номера: " + spot);
        }
        return bet;
    }

    public int payout() {
        return payout;
    }

    public Set<RouletteType> availableIn() {
        return availableIn;
    }

    public Set<Spot> winning() {
        return winning;
    }

}