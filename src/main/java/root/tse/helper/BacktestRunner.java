package root.tse.helper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import root.tse.configuration.properties.BacktestConfigurationProperties;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.backtest.BacktestExchangeGateway;
import root.tse.domain.backtest.BacktestService;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.strategy_execution.StrategyExecutionContext;
import root.tse.domain.strategy_execution.trade.TradeType;

import java.util.List;

// Note: backtest.enabled=true should be set in application.yml
//@Component
@RequiredArgsConstructor
public class BacktestRunner implements CommandLineRunner {

    // Important: data set should contain bar close timestamp for each bar.
    // Bar close timestamp is necessary in order to perform correct backtest of strategies
    // which use rules with multiple time-frames.
    // If data set has only open timestamp (as e.g. currency.com data set)
    // then it should be normalized by converting of open timestamp to close timestamp.
    // Script src/main/resources/sql/data-set-normalization.sql can be used as an example.

    private final ExchangeGateway exchangeGateway;
    private final BacktestConfigurationProperties backtestProperties;
    private final BacktestService backtestService;

    @Override
    @SneakyThrows
    public void run(String... args) {
        var backtestExchangeGateway = (BacktestExchangeGateway) exchangeGateway;
        var strategyExecutionContext = StrategyExecutionContext.builder()
            .entryRule(null) // set entry rule
            .exitRule(null) // set exit rule
            .tradeType(TradeType.LONG)
            .orderExecutionType(OrderExecutionType.STUB)
            .symbols(List.of(backtestProperties.getSymbol()))
            .fundsPerTrade(backtestProperties.getFundsPerTrade())
            .build();
        backtestService.runBacktest(strategyExecutionContext);
        var backtestReport = backtestService.getReport();

        printReportDelimiter();
        System.out.println("--------------------------------------------------");
        System.out.println("Report link: http://localhost:8080/backtest-report");
        System.out.println("--------------------------------------------------");
        System.out.println("Total profit: " + backtestReport.getTotalProfit());
        System.out.println("N closed trades: " + backtestReport.getNClosedTrades());
        System.out.println("N profitable trades: " + backtestReport.getNProfitableTrades());
        System.out.println("--------------------------------------------------");
        printReportDelimiter();
    }

    private void printReportDelimiter() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }
}
