package com.trading.roulette.strategy.strategies;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.utils.BetHelper;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Strategy(tags = {Tag.PHYSICAL, Tag.STATISTICAL})
public class PivotStrategy extends AbstractStrategy {
    private Spot currentPivot;
    private int betsPlacedOnPivot;
    private final List<Spot> observationBuffer;

    public PivotStrategy(Config config) {
        super(config);
        this.observationBuffer = new ArrayList<>();
        this.currentPivot = null;
        this.betsPlacedOnPivot = 0;

        // !!! ВАЖНО: Воспроизведение логики Пивота на истории !!!
        Deque<Spot> history = getMarket().getHistory();
        if (!history.isEmpty()) {
            for (Spot spot : history) {
                processPivotLogic(spot);
            }
        }
    }

    @Override
    public List<Bet> decide() {
        if (currentPivot != null) {
            long value = getMinBet();
            if (value > getVirtualBalance()) return List.of();
            return Bet.list(BetHelper.straightUp(currentPivot), value);
        }
        return List.of();
    }

    @Override
    protected void onRoundCustom(Spot spot, List<Bet> bets) {
        // Логика обновления состояния перенесена в processPivotLogic,
        // но здесь она вызывается "пост-фактум".
        // PivotStrategy в оригинале обновляется в decide(), что не совсем корректно для симуляции.
        // В правильной архитектуре обновление состояния должно быть здесь.
        processPivotLogic(spot);
    }

    // В оригинале логика была в decide(), что неправильно (состояние менялось до результата).
    // Правильнее обновлять состояние на основе выпавшего числа (outcome).
    private void processPivotLogic(Spot lastSpot) {
        if (currentPivot != null) {
            if (lastSpot == currentPivot) {
                // Победа! Сброс
                currentPivot = null;
                betsPlacedOnPivot = 0;
                observationBuffer.clear();
                observationBuffer.add(lastSpot);
            } else {
                betsPlacedOnPivot++;
                if (betsPlacedOnPivot >= 35) {
                    // Пивот отыграл 35 спинов без победы - сброс
                    currentPivot = null;
                    betsPlacedOnPivot = 0;
                }
            }
        }

        if (currentPivot == null) {
            if (observationBuffer.contains(lastSpot)) {
                // Число повторилось в буфере -> Новый Пивот
                currentPivot = lastSpot;
                betsPlacedOnPivot = 0;
                observationBuffer.clear();
            } else {
                observationBuffer.add(lastSpot);
                if (observationBuffer.size() > 36) { // Обычно буфер = 35-36 спинов
                    observationBuffer.removeFirst();
                }
            }
        }
    }
}