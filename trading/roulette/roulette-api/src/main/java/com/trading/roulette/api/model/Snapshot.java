package com.trading.roulette.api.model;

import com.trading.roulette.api.enums.Spot;

import java.util.List;

/**
 * Снимок состояния стратегии в конкретный момент времени (обычно после завершения раунда).
 * Используется для логирования, построения графиков и финального анализа эффективности.
 *
 * @param strategyName      Название стратегии (идентификатор).
 *                          <br>📝 <i>Пример:</i> "Martingale_Classic"
 * @param roundNumber       Текущий номер раунда (спина).
 *                          <br>🟢 <i>Хорошо:</i> Достигает {@code maxRoundsStop} (стратегия выжила).
 *                          <br>🔴 <i>Плохо:</i> Малое число (быстрый слив депозита).
 * @param realBalance           Текущий баланс игрока.
 *                          <br>📝 <i>Пример:</i> 10500
 * @param startBalance      Стартовый баланс. Точка отсчета для ROI и Profit.
 *                          <br>📝 <i>Пример:</i> 10000
 * @param lastProfit        Прибыль или убыток за <b>последний</b> раунд (PnL).
 *                          <br>🟢 <i>Хорошо:</i> Положительное значение.
 *                          <br>🔴 <i>Плохо:</i> Огромное отрицательное значение (означает срыв ставки в прогрессии).
 * @param netProfit         Чистая прибыль за всю игру ({@code Balance - StartBalance}).
 *                          <br>🟢 <i>Хорошо:</i> > 0.
 *                          <br>🔴 <i>Плохо:</i> < 0 или равно {@code -StartBalance} (полное банкротство).
 * @param roundBetAmount    Сумма всех ставок, сделанных в <b>текущем</b> раунде.
 *                          <br>🟢 <i>Хорошо:</i> Фиксированный или плавно меняющийся % от банка.
 *                          <br>🔴 <i>Плохо:</i> Экспоненциальный рост (признак агрессивного Мартингейла).
 * @param roiPercentage     Return on Investment (Рентабельность) в процентах.
 *                          <br>📝 <i>Формула:</i> {@code (NetProfit / StartBalance) * 100}
 *                          <br>🟢 <i>Хорошо:</i> > 10-20% при низкой просадке.
 *                          <br>🔴 <i>Плохо:</i> < 0% (убыток).
 * @param winRatePercentage Процент выигранных раундов.
 *                          <br>📝 <i>Формула:</i> {@code (Won / Rounds) * 100}
 *                          <br>📊 <i>Анализ:</i> Зависит от типа ставок. Для ставок на равные шансы (Красное/Черное) норма ~47.3-48.6%.
 *                          <br>🔴 <i>Подозрительно:</i> > 80% (обычно означает глубокие пересиживания убытков).
 * @param currentDrawdown   Текущая просадка в процентах от исторического пика баланса.
 *                          <br>📝 <i>Пример:</i> Если был пик 11000, а сейчас 10000, то DD = 9.09%.
 *                          <br>🟢 <i>Хорошо:</i> 0% (мы на пике прибыли).
 * @param maxDrawdown       Максимальная зафиксированная просадка за всю историю симуляции. Главный показатель риска.
 *                          <br>🟢 <i>Хорошо:</i> < 15-20%. Стратегия безопасна.
 *                          <br>🔴 <i>Плохо:</i> > 50%. Стратегия крайне рискованна, возможен слив в любой момент.
 * @param profitFactor      Фактор профита. Отношение всех полученных выплат к сумме всех ставок.
 *                          <br>📝 <i>Формула:</i> {@code TotalPayout / TotalWagered}
 *                          <br>🟢 <i>Отлично:</i> > 1.0 (Стратегия математически прибыльна на этом отрезке).
 *                          <br>⚪ <i>Нейтрально:</i> ~1.0 (Игра в ноль).
 *                          <br>🔴 <i>Плохо:</i> < 0.95 (Матожидание казино съедает депозит).
 * @param lastSpot          Последнее выпавшее число.
 *                          <br>📝 <i>Использование:</i> Для проверки, почему ставка сыграла или нет.
 * @param totalWon          Общее количество выигранных раундов.
 * @param totalLost         Общее количество проигранных раундов.
 * @param totalWagered      Оборот средств. Сумма всех сделанных ставок за игру.
 *                          <br>📝 <i>Пример:</i> Можно иметь баланс 10000, но оборот 1,000,000 (казино любит такие стратегии).
 * @param totalPayout       Общая сумма всех выплат от казино (включая возврат тела ставки при выигрыше).
 * @param lastBets          Список ставок, сделанных в последнем раунде.
 *                          <br>📝 <i>Пример:</i> {@code [RED: 10, ZERO: 1]}
 * @param isLive            Флаг остановки игры.
 *                          <br>📝 <i>Значение:</i> {@code true}, если сработал StopLoss, TakeProfit или закончились деньги.
 */
public record Snapshot(
        String strategyName,
        long roundNumber,
        long realBalance,
        long virtualBalance,
        long lastProfit,
        long netProfit,
        long roundBetAmount,
        double roiPercentage,
        double winRatePercentage,
        double currentDrawdown,
        double maxDrawdown,
        double profitFactor,
        Spot lastSpot,
        long betCount,
        long totalWon,
        long totalLost,
        long peakBalance,
        long totalWagered,
        long totalPayout,
        List<Bet> lastBets,
        long startBalance,
        boolean isLive,
        double sharpeRatio,
        int maxDrawdownDuration
) {
    /**
     * calculate field roiPercentage
     */
    public double getRoi() {
        return startBalance == 0 ? 0 : ((double) netProfit / startBalance) * 100;
    }

    /**
     * calculate field netProfit
     */
    public long getNetProfit() {
        return realBalance - startBalance;
    }

    /**
     * calculate field roiPercentage
     */
    public double getRoiPercentage() {
        if (startBalance == 0) return 0.0;
        return ((double) getNetProfit() / startBalance) * 100.0;
    }

    /**
     * calculate field winRatePercentage
     */
    public double getWinRatePercentage() {
        if (roundNumber == 0) return 0.0;
        return ((double) totalWon / roundNumber) * 100.0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String strategyName;
        private long roundNumber;
        private long realBalance;
        private long virtualBalance;
        private long lastProfit;
        private long netProfit;
        private long roundBetAmount;
        private double roiPercentage;
        private double winRatePercentage;
        private double currentDrawdown;
        private double maxDrawdown;
        private double profitFactor;
        private Spot lastSpot;
        private long betCount;
        private long totalWon;
        private long totalLost;
        private long peakBalance;
        private long totalWagered;
        private long totalPayout;
        private List<Bet> lastBets;
        private long startBalance;
        private boolean isLive;
        private double sharpeRatio;
        private int maxDrawdownDuration;

        public Snapshot build() {
            return new Snapshot(strategyName,
                    roundNumber,
                    realBalance,
                    virtualBalance,
                    lastProfit,
                    netProfit,
                    roundBetAmount,
                    roiPercentage,
                    winRatePercentage,
                    currentDrawdown,
                    maxDrawdown,
                    profitFactor,
                    lastSpot,
                    betCount,
                    totalWon,
                    totalLost,
                    peakBalance,
                    totalWagered,
                    totalPayout,
                    lastBets,
                    startBalance,
                    isLive,
             sharpeRatio,
            maxDrawdownDuration);
        }

        public Builder strategyName(String strategyName) {
            this.strategyName = strategyName;
            return this;
        }

        public Builder roundNumber(long roundNumber) {
            this.roundNumber = roundNumber;
            return this;
        }

        public Builder realBalance(long realBalance) {
            this.realBalance = realBalance;
            return this;
        }

        public Builder virtualBalance(long virtualBalance) {
            this.virtualBalance = virtualBalance;
            return this;
        }

        public Builder lastProfit(long lastProfit) {
            this.lastProfit = lastProfit;
            return this;
        }

        public Builder netProfit(long netProfit) {
            this.netProfit = netProfit;
            return this;
        }

        public Builder roundBetAmount(long roundBetAmount) {
            this.roundBetAmount = roundBetAmount;
            return this;
        }

        public Builder roiPercentage(double roiPercentage) {
            this.roiPercentage = roiPercentage;
            return this;
        }

        public Builder winRatePercentage(double winRatePercentage) {
            this.winRatePercentage = winRatePercentage;
            return this;
        }

        public Builder currentDrawdown(double currentDrawdown) {
            this.currentDrawdown = currentDrawdown;
            return this;
        }

        public Builder maxDrawdown(double maxDrawdown) {
            this.maxDrawdown = maxDrawdown;
            return this;
        }

        public Builder profitFactor(double profitFactor) {
            this.profitFactor = profitFactor;
            return this;
        }

        public Builder lastSpot(Spot lastSpot) {
            this.lastSpot = lastSpot;
            return this;
        }

        public Builder betCount(long betCount) {
            this.betCount = betCount;
            return this;
        }

        public Builder totalWon(long totalWon) {
            this.totalWon = totalWon;
            return this;
        }

        public Builder totalLost(long totalLost) {
            this.totalLost = totalLost;
            return this;
        }

        public Builder peakBalance(long peakBalance) {
            this.peakBalance = peakBalance;
            return this;
        }

        public Builder totalWagered(long totalWagered) {
            this.totalWagered = totalWagered;
            return this;
        }

        public Builder totalPayout(long totalPayout) {
            this.totalPayout = totalPayout;
            return this;
        }

        public Builder lastBets(List<Bet> lastBets) {
            this.lastBets = lastBets;
            return this;
        }

        public Builder startBalance(long startBalance) {
            this.startBalance = startBalance;
            return this;
        }

        public Builder isLive(boolean isLive) {
            this.isLive = isLive;
            return this;
        }
        public Builder sharpeRatio(double sharpeRatio) {
            this.sharpeRatio = sharpeRatio;
            return this;
        }
        public Builder maxDrawdownDuration(int maxDrawdownDuration) {
            this.maxDrawdownDuration = maxDrawdownDuration;
            return this;
        }
    }
}