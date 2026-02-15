package com.trading.roulette.api.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Spot {
    SPOT_0(0, Color.GREEN),
    SPOT_00(-1, Color.GREEN),
    SPOT_01(1, Color.RED),
    SPOT_02(2, Color.BLACK),
    SPOT_03(3, Color.RED),
    SPOT_04(4, Color.BLACK),
    SPOT_05(5, Color.RED),
    SPOT_06(6, Color.BLACK),
    SPOT_07(7, Color.RED),
    SPOT_08(8, Color.BLACK),
    SPOT_09(9, Color.RED),
    SPOT_10(10, Color.BLACK),
    SPOT_11(11, Color.BLACK),
    SPOT_12(12, Color.RED),
    SPOT_13(13, Color.BLACK),
    SPOT_14(14, Color.RED),
    SPOT_15(15, Color.BLACK),
    SPOT_16(16, Color.RED),
    SPOT_17(17, Color.BLACK),
    SPOT_18(18, Color.RED),
    SPOT_19(19, Color.RED),
    SPOT_20(20, Color.BLACK),
    SPOT_21(21, Color.RED),
    SPOT_22(22, Color.BLACK),
    SPOT_23(23, Color.RED),
    SPOT_24(24, Color.BLACK),
    SPOT_25(25, Color.RED),
    SPOT_26(26, Color.BLACK),
    SPOT_27(27, Color.RED),
    SPOT_28(28, Color.BLACK),
    SPOT_29(29, Color.BLACK),
    SPOT_30(30, Color.RED),
    SPOT_31(31, Color.BLACK),
    SPOT_32(32, Color.RED),
    SPOT_33(33, Color.BLACK),
    SPOT_34(34, Color.RED),
    SPOT_35(35, Color.BLACK),
    SPOT_36(36, Color.RED);

    private final int number;
    private final Color color;

    Spot(int number, Color color) {
        this.number = number;
        this.color = color;
    }

    public static final Set<Spot> REDS;
    public static final Set<Spot> BLACKS;
    public static final Set<Spot> GREENS;
    public static final Set<Spot> EVENS;
    public static final Set<Spot> ODDS;
    public static final Set<Spot> LOW_SPOTS;
    public static final Set<Spot> HIGH_SPOTS;
    public static final Set<Spot> FIRST_DOZEN_SPOTS;
    public static final Set<Spot> SECOND_DOZEN_SPOTS;
    public static final Set<Spot> THIRD_DOZEN_SPOTS;
    public static final Set<Spot> FIRST_COLUMN_SPOTS;
    public static final Set<Spot> SECOND_COLUMN_SPOTS;
    public static final Set<Spot> THIRD_COLUMN_SPOTS;

    private static final Set<Spot> FIRST_DOZEN_OR_ZERO_SPOTS;

    private static final Map<Integer, Spot> SPOT_BY_NUMBER =
            Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(Spot::number, s -> s));
    private static final Map<RouletteType, List<Spot>> AVAILABLE_SPOTS_CACHE;
    public static final Map<RouletteType, Spot[]> WHEEL_LAYOUTS;
    public static final Map<RouletteType, Map<Spot, Integer>> WHEEL_POSITIONS_CACHE;

    static {
        Map<RouletteType, List<Spot>> availableSpots = new EnumMap<>(RouletteType.class);
        Map<RouletteType, Spot[]> wheelLayouts = new EnumMap<>(RouletteType.class);
        Map<RouletteType, Map<Spot, Integer>> wheelPositions = new EnumMap<>(RouletteType.class);

        wheelLayouts.put(RouletteType.ONE_TO_36, new Spot[]{
                SPOT_01, SPOT_02, SPOT_03, SPOT_04, SPOT_05, SPOT_06, SPOT_07, SPOT_08, SPOT_09,
                SPOT_10, SPOT_11, SPOT_12, SPOT_13, SPOT_14, SPOT_15, SPOT_16, SPOT_17, SPOT_18,
                SPOT_19, SPOT_20, SPOT_21, SPOT_22, SPOT_23, SPOT_24, SPOT_25, SPOT_26, SPOT_27,
                SPOT_28, SPOT_29, SPOT_30, SPOT_31, SPOT_32, SPOT_33, SPOT_34, SPOT_35, SPOT_36
        });
        wheelLayouts.put(RouletteType.EUROPEAN, new Spot[]{
                SPOT_0, SPOT_32, SPOT_15, SPOT_19, SPOT_04, SPOT_21, SPOT_02, SPOT_25,
                SPOT_17, SPOT_34, SPOT_06, SPOT_27, SPOT_13, SPOT_36, SPOT_11, SPOT_30, SPOT_08,
                SPOT_23, SPOT_10, SPOT_05, SPOT_24, SPOT_16, SPOT_33, SPOT_01, SPOT_20, SPOT_14,
                SPOT_31, SPOT_09, SPOT_22, SPOT_18, SPOT_29, SPOT_07, SPOT_28, SPOT_12, SPOT_35,
                SPOT_03, SPOT_26
        });
        wheelLayouts.put(RouletteType.AMERICAN, new Spot[]{
                SPOT_0, SPOT_28, SPOT_09, SPOT_26, SPOT_30, SPOT_11, SPOT_07, SPOT_20, SPOT_32, SPOT_17,
                SPOT_05, SPOT_22, SPOT_34, SPOT_15, SPOT_03, SPOT_24, SPOT_36, SPOT_13, SPOT_01, SPOT_00,
                SPOT_27, SPOT_10, SPOT_25, SPOT_29, SPOT_12, SPOT_08, SPOT_19, SPOT_31, SPOT_18, SPOT_06,
                SPOT_21, SPOT_33, SPOT_16, SPOT_04, SPOT_23, SPOT_35, SPOT_14, SPOT_02
        });
        for (RouletteType type : RouletteType.values()) {
            availableSpots.put(type, availableSpotsUncached(type));
            Spot[] layout = wheelLayouts.get(type);
            if (layout != null) {
                Map<Spot, Integer> positions = new EnumMap<>(Spot.class);
                for (int i = 0; i < layout.length; i++) {
                    positions.put(layout[i], i);
                }
                wheelPositions.put(type, Collections.unmodifiableMap(positions));
            }
        }
        WHEEL_LAYOUTS = Collections.unmodifiableMap(wheelLayouts);
        AVAILABLE_SPOTS_CACHE = Collections.unmodifiableMap(availableSpots);
        WHEEL_POSITIONS_CACHE = Collections.unmodifiableMap(wheelPositions);

        EnumSet<Spot> reds = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> blacks = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> greens = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> evens = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> odds = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> lows = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> highs = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> firstDozen = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> secondDozen = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> thirdDozen = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> firstColumn = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> secondColumn = EnumSet.noneOf(Spot.class);
        EnumSet<Spot> thirdColumn = EnumSet.noneOf(Spot.class);

        for (Spot spot : values()) {
            if (spot.isRed()) reds.add(spot);
            if (spot.isBlack()) blacks.add(spot);
            if (spot.isGreen()) greens.add(spot);
            if (spot.isEven()) evens.add(spot);
            if (spot.isOdd()) odds.add(spot);
            if (spot.is1To18()) lows.add(spot);
            if (spot.is19To36()) highs.add(spot);
            if (spot.isFirstDozen()) firstDozen.add(spot);
            if (spot.isSecondDozen()) secondDozen.add(spot);
            if (spot.isThirdDozen()) thirdDozen.add(spot);
            if (spot.isFirstColumn()) firstColumn.add(spot);
            if (spot.isSecondColumn()) secondColumn.add(spot);
            if (spot.isThirdColumn()) thirdColumn.add(spot);
        }

        REDS = Collections.unmodifiableSet(reds);
        BLACKS = Collections.unmodifiableSet(blacks);
        GREENS = Collections.unmodifiableSet(greens);
        EVENS = Collections.unmodifiableSet(evens);
        ODDS = Collections.unmodifiableSet(odds);
        LOW_SPOTS = Collections.unmodifiableSet(lows);
        HIGH_SPOTS = Collections.unmodifiableSet(highs);
        FIRST_DOZEN_SPOTS = Collections.unmodifiableSet(firstDozen);
        SECOND_DOZEN_SPOTS = Collections.unmodifiableSet(secondDozen);
        THIRD_DOZEN_SPOTS = Collections.unmodifiableSet(thirdDozen);
        FIRST_COLUMN_SPOTS = Collections.unmodifiableSet(firstColumn);
        SECOND_COLUMN_SPOTS = Collections.unmodifiableSet(secondColumn);
        THIRD_COLUMN_SPOTS = Collections.unmodifiableSet(thirdColumn);

        FIRST_DOZEN_OR_ZERO_SPOTS = Collections.unmodifiableSet(EnumSet.of(
                SPOT_0, SPOT_00, SPOT_01, SPOT_02, SPOT_03, SPOT_04, SPOT_05,
                SPOT_06, SPOT_07, SPOT_08, SPOT_09, SPOT_10, SPOT_11, SPOT_12
        ));
    }

    public static Spot byNumber(int number) {
        Spot spot = SPOT_BY_NUMBER.get(number);
        if (spot == null) {
            throw new IllegalArgumentException("The number does not exist: " + number);
        }
        return spot;
    }

    private static List<Spot> availableSpotsUncached(RouletteType rouletteType) {
        Stream<Spot> stream = Arrays.stream(values());
        if (rouletteType == RouletteType.ONE_TO_36) {
            stream = stream.filter(s -> s != SPOT_0 && s != SPOT_00);
        } else if (rouletteType == RouletteType.EUROPEAN) {
            stream = stream.filter(s -> s != SPOT_00);
        }
        return stream.toList();
    }

    public static List<Spot> availableList(RouletteType rouletteType) {
        return AVAILABLE_SPOTS_CACHE.getOrDefault(rouletteType, List.of());
    }

    public boolean isRed() {
        return color == Color.RED;
    }

    public boolean isBlack() {
        return color == Color.BLACK;
    }

    public boolean isGreen() {
        return color == Color.GREEN;
    }

    public boolean isEven() {
        return number > 0 && number % 2 == 0;
    }

    public boolean isOdd() {
        return number > 0 && number % 2 != 0;
    }

    public boolean isFirstDozen() {
        return number >= 1 && number <= 12;
    }

    public boolean isSecondDozen() {
        return number >= 13 && number <= 24;
    }

    public boolean isThirdDozen() {
        return number >= 25 && number <= 36;
    }

    public boolean isFirstColumn() {
        return number > 0 && number % 3 == 1;
    }

    public boolean isSecondColumn() {
        return number > 0 && number % 3 == 2;
    }

    public boolean isThirdColumn() {
        return number > 0 && number % 3 == 0;
    }

    public boolean is1To18() {
        return number >= 1 && number <= 18;
    }

    public boolean is19To36() {
        return number >= 19 && number <= 36;
    }

    public int wheelPosition(RouletteType rouletteType) {
        return WHEEL_POSITIONS_CACHE.getOrDefault(rouletteType, Collections.emptyMap()).getOrDefault(this, -1);
    }

    public int physicalDistance(Spot spot, RouletteType rouletteType) {
        Objects.requireNonNull(spot, "spot cannot be null");
        int thisPos = wheelPosition(rouletteType);
        int otherPos = spot.wheelPosition(rouletteType);

        if (thisPos == -1 || otherPos == -1) {
            throw new IllegalArgumentException("One or both fields are not available in the specified roulette type");
        }

        int wheelSize = WHEEL_LAYOUTS.get(rouletteType).length;
        int diff = Math.abs(thisPos - otherPos);

        return Math.min(diff, wheelSize - diff);
    }

    public boolean isFirstDozenOrZeroAndDoubleZero() {
        return FIRST_DOZEN_OR_ZERO_SPOTS.contains(this);
    }

    public static Map<RouletteType, Spot[]> wheelLayouts() {
        return WHEEL_LAYOUTS;
    }

    public int number() {
        return number;
    }

    public Color color() {
        return color;
    }

    public boolean isZero() {
        return number == 0;
    }
}