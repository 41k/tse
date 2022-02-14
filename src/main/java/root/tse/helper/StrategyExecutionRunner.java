package root.tse.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.strategy_execution.MarketScanningStrategyExecutionFactory;
import root.tse.domain.strategy_execution.SimpleStrategyExecutionFactory;
import root.tse.domain.strategy_execution.StrategyExecution;
import root.tse.domain.strategy_execution.StrategyExecutionContext;
import root.tse.domain.strategy_execution.trade.TradeType;

import java.util.List;

//@Component
@RequiredArgsConstructor
public class StrategyExecutionRunner implements CommandLineRunner {

    private final ExchangeGateway exchangeGateway;
    private final SimpleStrategyExecutionFactory simpleStrategyExecutionFactory;
    private final MarketScanningStrategyExecutionFactory marketScanningStrategyExecutionFactory;

    private StrategyExecution strategyExecution;

    // Simple Strategy Execution
    @Override
    public void run(String... args) {
        var strategyExecutionContext = StrategyExecutionContext.builder()
            .entryRule(null) // set entry rule
            .exitRule(null) // set exit rule
            .tradeType(TradeType.LONG)
            .orderExecutionType(OrderExecutionType.STUB)
            .symbols(List.of("ETH/USD"))
            .fundsPerTrade(800d)
            .orderFeePercent(0.2d)
            .build();
        strategyExecution = simpleStrategyExecutionFactory.create(strategyExecutionContext);
        strategyExecution.start();
    }

//    // Market Scanning Strategy Execution
//    @Override
//    public void run(String... args) {
//        var strategyExecutionContext = StrategyExecutionContext.builder()
//            .entryRule(null) // set entry rule
//            .exitRule(null) // set exit rule
//            .tradeType(TradeType.LONG)
//            .orderExecutionType(OrderExecutionType.STUB)
//            .symbols(List.of("BTC/USD", "ETH/USD", "LTC/USD", "XRP/USD"))
//            .fundsPerTrade(1000d)
//            .orderFeePercent(0.2d)
//            .allowedNumberOfSimultaneouslyOpenedTrades(4)
//            .build();
//        strategyExecution = marketScanningStrategyExecutionFactory.create(strategyExecutionContext);
//        strategyExecution.start();
//    }
}
