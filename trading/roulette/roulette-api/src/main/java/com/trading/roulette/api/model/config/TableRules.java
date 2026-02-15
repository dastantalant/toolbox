package com.trading.roulette.api.model.config;

public record TableRules(boolean laPartage, boolean enPrison) {
    public static TableRules standard() {
        return new TableRules(false, false);
    }

    public static TableRules europeanFrench() {
        return new TableRules(true, false);
    }
}