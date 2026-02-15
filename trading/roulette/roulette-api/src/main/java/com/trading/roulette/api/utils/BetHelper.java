package com.trading.roulette.api.utils;

import com.trading.roulette.api.enums.BetType;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.SpotPrediction;
import com.trading.roulette.api.model.config.TableRules;

import java.util.List;

public final class BetHelper {
    private BetHelper() {
    }

    public static long totalPayout(List<Bet> list, Spot spot, TableRules rules) {
        if (list == null || list.isEmpty()) return 0;
        long payout = 0;
        for (Bet bet : list) {
            payout += calculatePayout(bet, spot, rules);
        }
        return payout;
    }

    private static long calculatePayout(Bet bet, Spot spot, TableRules rules) {
        boolean isWin = isWin(bet, spot);
        if (isWin) return bet.value() * bet.type().getReturnMultiplier();
        if (rules.laPartage() && spot.isZero() && isEvenMoneyBet(bet.type())) return bet.value() / 2;
        return 0;
    }

    private static boolean isEvenMoneyBet(BetType type) {
        return type.getPayout() == 1;
    }

    public static long totalBetValue(List<Bet> list) {
        return (list == null || list.isEmpty()) ? 0 : list.stream()
                .mapToLong(Bet::value)
                .sum();
    }

    public static long totalPayout(List<Bet> list, Spot spot) {
        return (list == null || list.isEmpty()) ? 0 : list.stream()
                .filter(bet -> isWin(bet, spot))
                .mapToLong(bet -> bet.value() * bet.type().getReturnMultiplier())
                .sum();
    }

    public static boolean hasWin(List<Bet> list, Spot spot) {
        return list != null && !list.isEmpty() && list.stream().anyMatch(bet -> isWin(bet, spot));
    }

    public static boolean isWin(Bet bet, Spot spot) {
        return isWin(bet.type(), spot);
    }

    public static boolean isWin(BetType type, Spot spot) {
        return type.getWinning().contains(spot);
    }

    public static BetType straightUp(Spot spot) {
        return BetType.getStraightUpBetFor(spot);
    }

    public static Bet betting(SpotPrediction spotPrediction, long amount) {
        return Bet.of(straightUp(spotPrediction.spot()), amount);
    }

    public static Bet betting(Spot spot, long amount) {
        return Bet.of(straightUp(spot), amount);
    }

    public static List<BetType> getAllCovering(Spot spot) {
        return BetType.getAllCovering(spot);
    }
}