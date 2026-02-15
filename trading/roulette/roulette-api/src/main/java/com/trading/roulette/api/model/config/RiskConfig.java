package com.trading.roulette.api.model.config;

import java.util.Objects;

/**
 * Конфигурация "Зонального Риск-менеджера" (ZoneRiskManager).
 * <p>
 * Этот класс определяет правила поведения системы защиты капитала, разделяя состояние баланса на три зоны:
 * <ol>
 *     <li><strong>Danger Zone (Опасная зона):</strong> Баланс упал ниже критического уровня. Ставки блокируются.</li>
 *     <li><strong>Normal Zone (Нормальная зона):</strong> Баланс находится около стартового значения. Размер ставок ограничен процентом от текущего банка.</li>
 *     <li><strong>Profit Zone (Зона прибыли):</strong> Баланс вырос выше целевого уровня. Ограничения на размер ставок снимаются (разрешается агрессивная игра на "деньги казино").</li>
 * </ol>
 *
 * @param dangerZoneThresholdRisk <strong>Порог Опасной Зоны (Danger Zone Threshold).</strong>
 *                                <p>
 *                                Коэффициент относительно <em>стартового</em> баланса.
 *                                Если текущий баланс становится меньше или равен {@code startBalance * dangerZoneThresholdRisk},
 *                                активируется режим "Штрафной бокс" (Penalty Box) — стратегия принудительно пропускает ходы.
 *                                <p>
 *                                <em>Цель:</em> Остановить "тильт" (серию необдуманных ставок) и переждать плохую серию ГСЧ.
 *                                <p>
 *                                <strong>Примеры:</strong>
 *                                <ul>
 *                                    <li>{@code 90} (Дефолт): Старт 1000$. Если баланс упадет до 900$ или ниже, ставки прекращаются на время {@code penaltyBoxDurationRisk}.</li>
 *                                    <li>{@code 50} (Агрессивный): Позволяет проиграть половину банка (до 500$) перед паузой.</li>
 *                                    <li>{@code 95} (Консервативный): Пауза при малейшей просадке в 5% (ниже 950$).</li>
 *                                </ul>
 * @param profitZoneThresholdRisk <strong>Порог Зоны Прибыли (Profit Zone Threshold).</strong>
 *                                <p>
 *                                Коэффициент относительно <em>стартового</em> баланса.
 *                                Если текущий баланс превышает {@code startBalance * profitZoneThresholdRisk},
 *                                стратегия считается вышедшей в зону сверхприбыли.
 *                                <p>
 *                                <em>Эффект:</em> Снимается ограничение {@code normalZoneBetCapRisk}. Стратегия может делать ставки любого размера
 *                                (в пределах лимитов стола {@code tableLimit}). Это позволяет стратегиям с позитивной прогрессией (Paroli)
 *                                разгонять депозит.
 *                                <p>
 *                                <strong>Примеры:</strong>
 *                                <ul>
 *                                    <li>{@code 110} (Дефолт): Старт 1000$. При балансе > 1100$ ограничения снимаются.</li>
 *                                    <li>{@code 200}: Ограничения снимутся только после удвоения депозита (до 2000$).</li>
 *                                    <li>{@code 101}: Практически сразу разрешает агрессивную игру (как только вышли в +1%).</li>
 *                                </ul>
 * @param normalZoneBetCapRisk    <strong>Лимит ставки в Нормальной Зоне (Normal Zone Bet Cap).</strong>
 *                                <p>
 *                                Доля от <em>текущего</em> (не стартового!) баланса, которую разрешено поставить за один раунд,
 *                                пока мы находимся между {@code Danger} и {@code Profit} зонами.
 *                                <p>
 *                                Если стратегия пытается поставить больше, {@code RiskManager} пропорционально уменьшит (масштабирует)
 *                                все ставки так, чтобы их сумма укладывалась в лимит.
 *                                <p>
 *                                <strong>Примеры:</strong>
 *                                <ul>
 *                                    <li>{@code 5} (Дефолт, 5%): Текущий баланс 1000$. Максимальная суммарная ставка = 50$.
 *                                        <br>Если стратегия хочет поставить 100$, ставки будут уменьшены в 2 раза.</li>
 *                                    <li>{@code 1} (Сейф, 1%): Очень жесткий контроль. При 1000$ макс ставка 10$. Подходит для длинных дистанций.</li>
 *                                    <li>{@code 25} (Риск, 25%): Разрешает ставить четверть банка за раз. Высокий риск слива.</li>
 *                                </ul>
 * @param penaltyBoxDurationRisk  <strong>Длительность "Штрафного бокса" (Penalty Box Duration).</strong>
 *                                <p>
 *                                Количество раундов, которые стратегия будет пропускать (не делать ставок),
 *                                если баланс упал в Опасную Зону (ниже {@code dangerZoneThresholdRisk}).
 *                                <p>
 *                                По истечении этого времени стратегия снова попробует сделать ставку.
 *                                Если баланс всё еще в опасной зоне, пауза активируется снова.
 *                                <p>
 *                                <strong>Примеры:</strong>
 *                                <ul>
 *                                    <li>{@code 10} (Дефолт): Пропустить 10 спинов. Достаточно, чтобы сменился локальный тренд.</li>
 *                                    <li>{@code 1}: Почти не останавливается, просто пропускает 1 ход.</li>
 *                                    <li>{@code 100}: Долгая пауза, ожидание полной смены условий (или дилера).</li>
 *                                </ul>
 */

public record RiskConfig(
        Integer dangerZoneThresholdRisk,
        Integer profitZoneThresholdRisk,
        Integer normalZoneBetCapRisk,
        Integer penaltyBoxDurationRisk,
        boolean strictMode

) {

    /**
     * Конструктор с инициализацией значений по умолчанию.
     *
     * @param dangerZoneThresholdRisk порог опасности (default: 90)
     * @param profitZoneThresholdRisk порог профита (default: 110)
     * @param normalZoneBetCapRisk    лимит ставки (default: 5)
     * @param penaltyBoxDurationRisk  штрафные раунды (default: 10)
     */
    public RiskConfig(Integer dangerZoneThresholdRisk, Integer profitZoneThresholdRisk, Integer normalZoneBetCapRisk, Integer penaltyBoxDurationRisk, boolean strictMode) {
        this.dangerZoneThresholdRisk = Objects.requireNonNullElse(dangerZoneThresholdRisk, 90); // 90%
        this.profitZoneThresholdRisk = Objects.requireNonNullElse(profitZoneThresholdRisk, 110); // 110%
        int rawCap = Objects.requireNonNullElse(normalZoneBetCapRisk, 5);
        this.normalZoneBetCapRisk = Math.min(rawCap, 100);
        this.penaltyBoxDurationRisk = Objects.requireNonNullElse(penaltyBoxDurationRisk, 10);
        boolean isDisabledMode = this.dangerZoneThresholdRisk == 0 && this.profitZoneThresholdRisk == 0;
        this.strictMode = strictMode;
        if (!isDisabledMode && (this.dangerZoneThresholdRisk >= this.profitZoneThresholdRisk)) {
            throw new IllegalArgumentException("Danger threshold must be lower than Profit threshold");
        }
    }

    public static RiskConfig defaultConfig() {
        return new RiskConfig(90, 110, 5, 10, false);
    }

    /**
     * Конфигурация ОТКЛЮЧЕННОГО Риск-менеджера.
     * <p>
     * Позволяет стратегии играть без ограничений:
     * 1. Danger = 0.0 (пауза только при банкротстве).
     * 2. Profit = 0.0 (всегда считаем, что мы в зоне свободы, лимиты сняты).
     * 3. Cap = 1.0 (можно ставить 100% банка).
     * 4. Penalty = 0 (нет пауз).
     * <p>
     * <em>Примечание: Лимит стола (Table Limit) всё равно будет соблюдаться,
     * так как это физическое ограничение казино.</em>
     */

    public static RiskConfig disabled() {
        return new RiskConfig(0, 0, 100, 0, false);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer dangerZoneThresholdRisk;
        private Integer profitZoneThresholdRisk;
        private Integer normalZoneBetCapRisk;
        private Integer penaltyBoxDurationRisk;
        private boolean strictMode;

        public RiskConfig build() {
            return new RiskConfig(dangerZoneThresholdRisk, profitZoneThresholdRisk, normalZoneBetCapRisk, penaltyBoxDurationRisk, strictMode);
        }

        public Builder dangerZoneThresholdRisk(Integer dangerZoneThresholdRisk) {
            this.dangerZoneThresholdRisk = dangerZoneThresholdRisk;
            return this;
        }

        public Builder profitZoneThresholdRisk(Integer profitZoneThresholdRisk) {
            this.profitZoneThresholdRisk = profitZoneThresholdRisk;
            return this;
        }

        public Builder normalZoneBetCapRisk(Integer normalZoneBetCapRisk) {
            this.normalZoneBetCapRisk = normalZoneBetCapRisk;
            return this;
        }

        public Builder penaltyBoxDurationRisk(Integer penaltyBoxDurationRisk) {
            this.penaltyBoxDurationRisk = penaltyBoxDurationRisk;
            return this;
        }

        public Builder strictMode(boolean strictMode) {
            this.strictMode = strictMode;
            return this;
        }
    }
}
