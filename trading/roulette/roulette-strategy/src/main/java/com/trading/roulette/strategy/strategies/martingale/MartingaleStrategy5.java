package com.trading.roulette.strategy.strategies.martingale;

import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;

import java.util.List;
import static com.trading.roulette.api.enums.BetType.*;

@Strategy(tags = {Tag.PROGRESSIVE})
public class MartingaleStrategy5 extends AbstractMartingaleStrategy {

    public MartingaleStrategy5(Config config) {
        super(config, List.of(SPOT_02, SPOT_03, SPOT_04, SPOT_05, SPOT_06, SPOT_07, SPOT_08, SPOT_09, SPOT_10, SPOT_11,
                SPOT_12, SPOT_13, SPOT_14, SPOT_15, SPOT_16, SPOT_17, SPOT_18, SPOT_19, SPOT_20, SPOT_21, SPOT_22,
                SPOT_23, SPOT_24, SPOT_25, SPOT_26, SPOT_27, SPOT_28, SPOT_29, SPOT_30, SPOT_31, SPOT_32, SPOT_33,
                SPOT_34, SPOT_35, SPOT_36));
    }

    @Override
    protected long calculateNext(Config config) {
        return getLastBets().getFirst().value() * 18;
    }
}
