package com.trading.roulette.strategy.strategies.manager;

import com.trading.roulette.api.IStrategy;
import com.trading.roulette.api.AbstractStrategy;
import com.trading.roulette.api.annotation.Tag;
import com.trading.roulette.api.model.config.Config;
import com.trading.roulette.api.enums.Spot;
import com.trading.roulette.api.annotation.Strategy;
import com.trading.roulette.api.model.Bet;
import com.trading.roulette.api.utils.HistoryUtil;
import com.trading.roulette.strategy.strategies.MeanReversionStrategy;
import com.trading.roulette.strategy.strategies.ParoliStrategy;
import com.trading.roulette.strategy.strategies.regime.AbstractRegimeStrategy;
import com.trading.roulette.strategy.strategies.regime.DoubleTriple;
import com.trading.roulette.strategy.strategies.regime.RegimeContext;

import java.util.List;

@Strategy(tags = {Tag.PATTERN, Tag.HYBRID})
public class EnhancedMarketRegimeStrategy extends AbstractRegimeStrategy {

    private final IStrategy trendFollowingStrategy;
    private final IStrategy meanReversionStrategy;

    public EnhancedMarketRegimeStrategy(Config config) {
        super(config, RegimeContext.builder()
                .trendThreshold(0.65)
                .volatilityLowThreshold(6.0)
                .build());

        trendFollowingStrategy = new ParoliStrategy(config);
        meanReversionStrategy = new MeanReversionStrategy(config);
    }

    @Override
    public List<Bet> decide() {
        var history = getState().getMarket().getHistory();
        int size = history.size();
        if (size < historyWindow) {
            return List.of();
        }
        DoubleTriple metrics = calculateMetrics(HistoryUtil.window(history, historyWindow));
        double redRate = metrics.getFirst();
        boolean isTrendRegime = (redRate >= trendThreshold) || (redRate <= (1.0 - trendThreshold));
        IStrategy activeStrategy = isTrendRegime ? trendFollowingStrategy : meanReversionStrategy;
        if (activeStrategy instanceof AbstractStrategy bs) {
            bs.setVirtualBalance(this.getVirtualBalance());
        }
        return activeStrategy.decide();
    }

    @Override
    protected void onRoundCustom(Spot outcome, List<Bet> realBets) {
        updateSubStrategy(trendFollowingStrategy, realBets, outcome);
        updateSubStrategy(meanReversionStrategy, realBets, outcome);
    }

    private void updateSubStrategy(IStrategy strategy, List<Bet> bets, Spot outcome) {
        if (strategy instanceof AbstractStrategy baseStrategy) {
            baseStrategy.setVirtualBalance(this.getVirtualBalance());
            baseStrategy.commitResult( outcome,bets);
        }
    }
}