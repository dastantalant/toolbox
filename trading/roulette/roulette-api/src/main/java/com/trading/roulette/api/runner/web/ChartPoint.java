package com.trading.roulette.api.runner.web;


/**
 * Точка графика для UI.
 *
 * @param round номер раунда
 * @param balance текущий баланс
 * @param profit изменение баланса за раунд ( >0 - выигрыш, <0 - проигрыш)
 * @param betsInfo компактное описание ставок (например: "RED:100, ZERO:5")
 */
public record ChartPoint(
        long round,
        long balance,
        long profit
) {}