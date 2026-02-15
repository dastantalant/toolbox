package com.trading.roulette.api.model.config;

/**
 * Конфигурация условий аварийной или плановой остановки стратегии.
 * Определяет границы рисков и целевые показатели.
 * <p>
 * Если поле равно null или 0 (для некоторых параметров), соответствующее условие считается отключенным.
 *
 * @param stopLossPercentageStop     <strong>Stop Loss (Ограничение убытка) в процентах от начального баланса.</strong>
 *                                   <p>
 *                                   Если текущий баланс упадет ниже этого уровня относительно старта, игра остановится.
 *                                   <br>
 *                                   <em>Пример:</em> Стартовый баланс 1000, stopLossPercentageStop = 20.
 *                                   Условие сработает, если баланс станет <= 800 (1000 - 20%).
 * @param takeProfitPercentageStop   <strong>Take Profit (Фиксация прибыли) в процентах от начального баланса.</strong>
 *                                   <p>
 *                                   Если текущий баланс вырастет выше этого уровня относительно старта, игра остановится.
 *                                   <br>
 *                                   <em>Пример:</em> Стартовый баланс 1000, takeProfitPercentageStop = 50.
 *                                   Условие сработает, если баланс станет >= 1500 (1000 + 50%).
 * @param trailingStopPercentageStop <strong>Trailing Stop (Скользящий стоп) в процентах от пикового баланса.</strong>
 *                                   <p>
 *                                   Фиксирует прибыль. Если баланс упадет на указанный процент от <strong>максимально достигнутого</strong>
 *                                   за время игры значения, стратегия остановится.
 *                                   <br>
 *                                   <em>Пример:</em> Баланс поднялся до 2000 (пик). trailingStopPercentageStop = 10.
 *                                   Условие сработает, если баланс откатится до 1800 (2000 - 10%).
 * @param maxDrawdownPercentageStop  <strong>Максимальная допустимая просадка (Max Drawdown) в процентах.</strong>
 *                                   <p>
 *                                   Это условие останавливает стратегию, если текущий баланс снизился относительно
 *                                   <strong>максимального значения (Peak Balance)</strong>, достигнутого за всё время игры,
 *                                   более чем на указанный процент.
 *                                   <p>
 *                                   Это механизм защиты накопленной прибыли. В отличие от {@code stopLossPercentageStop} (который считает от стартового баланса),
 *                                   этот параметр динамически отслеживает вашу "вершину".
 *                                   <p>
 *                                   <strong>Формула срабатывания:</strong>
 *                                   <br>
 *                                   {@code (Пиковый_Баланс - Текущий_Баланс) >= (Пиковый_Баланс * Процент / 100)}
 *                                   <p>
 *                                   <em>Пример:</em>
 *                                   <ul>
 *                                       <li>Старт: 1000$. {@code maxDrawdownPercentageStop} = 25%.</li>
 *                                       <li>Вы играете и выигрываете, баланс растет: 1200... 1500... <strong>2000$</strong>.</li>
 *                                       <li>2000$ — это ваш текущий Пик (Peak Balance).</li>
 *                                       <li>Допустимая просадка: 25% от 2000$ = 500$.</li>
 *                                       <li>Порог остановки: 2000$ - 500$ = <strong>1500$</strong>.</li>
 *                                       <li>Если начнется серия неудач и баланс упадет до 1500$ или ниже, стратегия остановится.</li>
 *                                   </ul>
 * @param maxWinStreakStop           <strong>Максимальная серия побед (Win Streak).</strong>
 *                                   <p>
 *                                   Остановка после указанного количества выигранных раундов подряд.
 *                                   Полезно для стратегий "Hit and Run" (забрал и ушел).
 *                                   <br>
 *                                   <em>Пример:</em> maxWinStreakStop = 5. Победа -> Победа -> Победа -> Победа -> Победа -> СТОП.
 * @param maxLossStreakStop          <strong>Максимальная серия поражений (Loss Streak).</strong>
 *                                   <p>
 *                                   Остановка после указанного количества проигранных раундов подряд.
 *                                   Предохранитель для мартингейл-подобных стратегий.
 *                                   <br>
 *                                   <em>Пример:</em> maxLossStreakStop = 6. Шесть поражений подряд вызовут остановку.
 * @param maxRoundsStop              <strong>Лимит количества раундов.</strong>
 *                                   <p>
 *                                   Жесткое ограничение на длительность игровой сессии.
 *                                   <br>
 *                                   <em>Пример:</em> maxRoundsStop = 1000. После 1000-го спина игра завершится независимо от результата.
 * @param stagnationWindowStop       <strong>Окно стагнации (количество раундов).</strong>
 *                                   <p>
 *                                   Остановка, если баланс не изменяется в течение указанного числа раундов
 *                                   (например, стратегия перестала делать ставки из-за внутренних фильтров).
 *                                   <br>
 *                                   <em>Пример:</em> stagnationWindowStop = 50. Если 50 раундов подряд баланс один и тот же -> СТОП.
 * @param minWinRateThresholdStop    <strong>Минимальный порог Винрейта (Win Rate).</strong>
 *                                   <p>
 *                                   Значение от 0.0 до 1.0. Если процент побед падает ниже этого уровня, игра останавливается.
 *                                   <br>
 *                                   <em>Пример:</em> minWinRateThresholdLimit = 0.45 (45%).
 * @param minRoundsForWinRateStop    <strong>Период "разогрева" для проверки Винрейта.</strong>
 *                                   <p>
 *                                   Количество раундов, в течение которых проверка `minWinRateThresholdLimit` игнорируется.
 *                                   Позволяет стратегии набрать статистику, чтобы не остановиться после первых пары неудач.
 *                                   <br>
 *                                   <em>Пример:</em> minRoundsForWinRateStop = 100. Проверка на низкий винрейт начнется только со 101-го раунда.
 * @param equityCurveSmaPeriodStop   <strong>Период SMA для проверки кривой эквити (Equity Curve Stop).</strong>
 *                                   <p>
 *                                   Если задано значение > 0, включается механизм остановки по ухудшению скользящей средней кривой баланса.
 *                                   Стратегия останавливается, если <strong>текущий баланс</strong> падает ниже 95% от
 *                                   <strong>простой скользящей средней (SMA)</strong> баланса за последние N раундов.
 *                                   <p>
 *                                   Это помогает ловить ситуации, когда стратегия начинает устойчиво деградировать,
 *                                   даже если абсолютная просадка ещё не достигла уровня maxDrawdown или trailing stop.
 *                                   <br>
 *                                   Очень полезно для фильтрации «медленных сливов» и затяжных боковиков с отрицательным матожиданием.
 *                                   <p>
 *                                   <strong>Логика срабатывания:</strong>
 *                                   <br>
 *                                   1. Собираем историю баланса за последние {@code equityCurveSmaPeriodStop} раундов
 *                                   2. Считаем SMA (простую среднюю) этих значений
 *                                   3. Если текущий баланс < SMA × 0.95 → стоп
 *                                   <p>
 *                                   <em>Пример:</em>
 *                                   <ul>
 *                                       <li>{@code equityCurveSmaPeriodStop = 50}</li>
 *                                       <li>Последние 50 раундов баланс колебался в диапазоне 1800–2200</li>
 *                                       <li>SMA за 50 периодов ≈ 1980</li>
 *                                       <li>95% от SMA = 1980 × 0.95 ≈ 1881</li>
 *                                       <li>Если текущий баланс упадёт ниже ~1881 — стратегия остановится</li>
 *                                   </ul>
 *                                   <p>
 *                                   <strong>Значения:</strong>
 *                                   <ul>
 *                                       <li>{@code null} или ≤ 0 — условие отключено</li>
 *                                       <li>обычно используют 20–200 в зависимости от стиля стратегии</li>
 *                                       <li>слишком маленькое значение (5–10) → много ложных срабатываний</li>
 *                                       <li>слишком большое (500+) → реакция слишком запаздывает</li>
 *                                   </ul>
 */
public record StopConditionConfig(
        Integer stopLossPercentageStop,
        Integer takeProfitPercentageStop,
        Integer trailingStopPercentageStop,
        Integer maxDrawdownPercentageStop,
        Integer maxWinStreakStop,
        Integer maxLossStreakStop,
        Long maxRoundsStop,
        Integer stagnationWindowStop,
        Integer minWinRateThresholdStop,
        Long minRoundsForWinRateStop,
        Integer equityCurveSmaPeriodStop
) {
    public StopConditionConfig {
        stopLossPercentageStop = clamp(stopLossPercentageStop, 0, 100);
        takeProfitPercentageStop = clamp(takeProfitPercentageStop, 0, null);
        trailingStopPercentageStop = clamp(trailingStopPercentageStop, 0, 100);
        maxDrawdownPercentageStop = clamp(maxDrawdownPercentageStop, 0, 100);
        maxWinStreakStop = clamp(maxWinStreakStop, 0, null);
        maxLossStreakStop = clamp(maxLossStreakStop, 0, null);
        maxRoundsStop = (maxRoundsStop == null || maxRoundsStop <= 0) ? null : maxRoundsStop;
        stagnationWindowStop = clamp(stagnationWindowStop, 0, null);
        minWinRateThresholdStop = clamp(minWinRateThresholdStop, 0, 100);
        minRoundsForWinRateStop = (minRoundsForWinRateStop == null || minRoundsForWinRateStop < 0) ? 100L : minRoundsForWinRateStop;
        equityCurveSmaPeriodStop = clamp(equityCurveSmaPeriodStop, 1, 1000);
    }

    private static Integer clamp(Integer val, Integer min, Integer max) {
        if (val == null) return 0;
        if (min != null && val < min) return min;
        if (max != null && val > max) return max;
        return val;
    }

    public static StopConditionConfig of() {
        return StopConditionConfig.builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer stopLossPercentageStop;
        private Integer takeProfitPercentageStop;
        private Integer trailingStopPercentageStop;
        private Integer maxDrawdownPercentageStop;
        private Integer maxWinStreakStop;
        private Integer maxLossStreakStop;
        private Long maxRoundsStop;
        private Integer stagnationWindowStop;
        private Integer minWinRateThresholdStop;
        private Long minRoundsForWinRateStop;
        private Integer equityCurveSmaPeriodStop;

        public StopConditionConfig build() {
            return new StopConditionConfig(
                    stopLossPercentageStop,
                    takeProfitPercentageStop,
                    trailingStopPercentageStop,
                    maxDrawdownPercentageStop,
                    maxWinStreakStop,
                    maxLossStreakStop,
                    maxRoundsStop,
                    stagnationWindowStop,
                    minWinRateThresholdStop,
                    minRoundsForWinRateStop,
                    equityCurveSmaPeriodStop
            );
        }

        public Builder stopLossPercentageStop(Integer stopLossPercentageStop) {
            this.stopLossPercentageStop = stopLossPercentageStop;
            return this;
        }

        public Builder takeProfitPercentageStop(Integer takeProfitPercentageStop) {
            this.takeProfitPercentageStop = takeProfitPercentageStop;
            return this;
        }

        public Builder trailingStopPercentageStop(Integer trailingStopPercentageStop) {
            this.trailingStopPercentageStop = trailingStopPercentageStop;
            return this;
        }

        public Builder maxDrawdownPercentageStop(Integer maxDrawdownPercentageStop) {
            this.maxDrawdownPercentageStop = maxDrawdownPercentageStop;
            return this;
        }

        public Builder maxWinStreakStop(Integer maxWinStreakStop) {
            this.maxWinStreakStop = maxWinStreakStop;
            return this;
        }

        public Builder maxLossStreakStop(Integer maxLossStreakStop) {
            this.maxLossStreakStop = maxLossStreakStop;
            return this;
        }

        public Builder maxRoundsStop(Long maxRoundsStop) {
            this.maxRoundsStop = maxRoundsStop;
            return this;
        }

        public Builder stagnationWindowStop(Integer stagnationWindowStop) {
            this.stagnationWindowStop = stagnationWindowStop;
            return this;
        }

        public Builder minWinRateThresholdStop(Integer minWinRateThresholdStop) {
            this.minWinRateThresholdStop = minWinRateThresholdStop;
            return this;
        }

        public Builder minRoundsForWinRateStop(Long minRoundsForWinRateStop) {
            this.minRoundsForWinRateStop = minRoundsForWinRateStop;
            return this;
        }

        public Builder equityCurveSmaPeriodStop(Integer equityCurveSmaPeriodStop) {
            this.equityCurveSmaPeriodStop = equityCurveSmaPeriodStop;
            return this;
        }
    }
}
