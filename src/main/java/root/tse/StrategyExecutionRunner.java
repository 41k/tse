package root.tse;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import root.tse.domain.strategy.SimpleBreakoutStrategy;
import root.tse.domain.strategy_execution.*;

import java.util.List;

import static root.tse.domain.strategy_execution.StrategyExecutionMode.INCUBATION;

//@Component
@RequiredArgsConstructor
public class StrategyExecutionRunner implements CommandLineRunner {

    private final ExchangeGateway exchangeGateway;
    private final MarketScanningStrategyExecutionFactory strategyExecutionFactory;

    private StrategyExecution strategyExecution;

    @Override
    public void run(String... args) {
        var strategy = new SimpleBreakoutStrategy(Interval.ONE_MINUTE, 3, 0.65, 2, exchangeGateway);
        var strategyExecutionContext = StrategyExecutionContext.builder()
            .strategy(strategy)
            .strategyExecutionMode(INCUBATION)
            .symbols(List.of("BTC/USD", "ETH/USD", "LTC/USD", "XRP/USD"))
            .fundsPerTrade(1000d)
            .transactionFeePercent(0.2d)
            .allowedNumberOfSimultaneouslyOpenedTrades(4)
            .build();
        strategyExecution = strategyExecutionFactory.create(strategyExecutionContext);
        strategyExecution.start();
    }
}
