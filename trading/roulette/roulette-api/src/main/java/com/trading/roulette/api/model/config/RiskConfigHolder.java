package com.trading.roulette.api.model.config;

public class RiskConfigHolder {
    /**
     * 1. СТАНДАРТНЫЙ (Balanced)
     * ----------------------------------------------------------------
     * Подходит для большинства стратегий (Марков, Паттерны).
     * Дает небольшое пространство для просадки (10%), прежде чем остановить игру.
     * Снимает ограничения после получения ощутимой прибыли (10%).
     */
    public static final RiskConfig BALANCED = RiskConfig.builder()
            .dangerZoneThresholdRisk(90) // Пауза, если баланс упал ниже 90% (потеряли 10%)
            .profitZoneThresholdRisk(110) // Свобода ставок, если баланс выше 110% (заработали 10%)
            .normalZoneBetCapRisk(5)    // В обычной зоне ставим не более 5% от банка
            .penaltyBoxDurationRisk(10)    // "Остываем" 10 раундов при просадке
            .build();

    /**
     * 2. КОНСЕРВАТИВНЫЙ (Iron Bank / Safe)
     * ----------------------------------------------------------------
     * Для игры на длинную дистанцию. Главная цель — не потерять депозит.
     * Пауза включается при малейшем убытке. Ставки мизерные.
     * Штраф (пауза) очень долгий, чтобы переждать "плохого дилера".
     */
    public static final RiskConfig CONSERVATIVE = RiskConfig.builder()
            .dangerZoneThresholdRisk(95) // Паника, если потеряли всего 5% от старта
            .profitZoneThresholdRisk(105) // Разгон разрешен после +5% профита
            .normalZoneBetCapRisk(1)    // Ставка строго не больше 1% от банка (менеджмент 1/100)
            .penaltyBoxDurationRisk(50)    // Если пошла просадка — сидим и смотрим 50 спинов
            .build();

    /**
     * 3. АГРЕССИВНЫЙ (High Roller / Kamikaze)
     * ----------------------------------------------------------------
     * Для стратегий с высокой волатильностью (Мартингейл, Келли).
     * Позволяет глубокие просадки (до половины банка), чтобы стратегия успела отыграться.
     * Разрешает огромные ставки (20% банка за раз).
     */
    public static final RiskConfig AGGRESSIVE = RiskConfig.builder()
            .dangerZoneThresholdRisk(50) // Терпим просадку до -50% перед остановкой
            .profitZoneThresholdRisk(150) // "Зона свободы" только после +50% профита
            .normalZoneBetCapRisk(20)    // Разрешено ставить 1/5 банка за один спин!
            .penaltyBoxDurationRisk(3)     // Пауза короткая, быстро возвращаемся в бой
            .build();

    /**
     * 4. СКАЛЬПЕР (Quick Scalper)
     * ----------------------------------------------------------------
     * Для быстрого выхода в безубыток.
     * Очень узкий коридор. Как только мы вышли в минимальный плюс (+1%),
     * риск-менеджер снимает все ограничения, позволяя Paroli работать на полную.
     */
    public static final RiskConfig SCALPER = RiskConfig.builder()
            .dangerZoneThresholdRisk(85) // Стандартная защита снизу (-15%)
            .profitZoneThresholdRisk(101) // СНИМАЕМ ОГРАНИЧЕНИЯ СРАЗУ после +1% прибыли
            .normalZoneBetCapRisk(5)    // 5% кап на старте
            .penaltyBoxDurationRisk(5)
            .build();

    /**
     * 5. ГРИНДЕР (The Grinder / Deep Recovery)
     * ----------------------------------------------------------------
     * Для стратегий типа Labouchere или TenUnit.
     * Позволяет уходить в минус (до 70%), но при этом держит ставки в жестких рамках (2%),
     * чтобы не слить остаток одной ставкой.
     */
    public static final RiskConfig GRINDER = RiskConfig.builder()
            .dangerZoneThresholdRisk(70) // Разрешаем просадку 30%
            .profitZoneThresholdRisk(120) // Цель +20%
            .normalZoneBetCapRisk(2)    // Но ставим понемногу (2%), чтобы выжить в просадке
            .penaltyBoxDurationRisk(15)
            .build();

    /**
     * 6. "ПЕРЕВЕРТЫШ" (Wide Range)
     * ----------------------------------------------------------------
     * Почти отключает зоны.
     * Danger Zone очень низко (по сути Stop Loss), Profit Zone очень высоко.
     * Весь контроль осуществляется через betCap (фиксированный процент).
     */
    public static final RiskConfig WIDE_RANGE = RiskConfig.builder()
            .dangerZoneThresholdRisk(10) // Пауза только если осталось 10% от банка (почти смерть)
            .profitZoneThresholdRisk(500) // Ограничения снимутся только при x5 (почти никогда)
            .normalZoneBetCapRisk(3)    // Всю игру мы жестко ограничены 3% ставкой
            .penaltyBoxDurationRisk(0)     // Паузы нет (0 раундов)
            .build();
}