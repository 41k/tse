package root.tse.helper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import root.tse.domain.backtest.BacktestContext;
import root.tse.domain.backtest.BacktestExchangeGateway;
import root.tse.domain.backtest.BacktestService;
import root.tse.domain.backtest.DataSetService;
import root.tse.domain.strategy.SimpleBreakoutStrategy;
import root.tse.domain.clock.Interval;

//@Component
@RequiredArgsConstructor
public class BacktestRunner implements CommandLineRunner {

    // Important: data set should contain bar close timestamp for each bar.
    // Bar close timestamp is necessary in order to perform correct backtest of strategies
    // which use rules with multiple time-frames.
    // If data set has only open timestamp (as e.g. currency.com data set)
    // then it should be normalized by converting of open timestamp to close timestamp.
    // Script src/main/resources/sql/data-set-normalization.sql can be used as an example.
    private static final String DATA_SET_NAME = "normalized_data_set_1";
    private static final String SYMBOL = "ETH_USD";
    private static final Double FUNDS_PER_TRADE = 1000d;
    private static final Double ORDER_FEE_PERCENT = 0.2d;

    private final DataSetService dataSetService;
    private final BacktestService backtestService;

    @Override
    @SneakyThrows
    public void run(String... args) {
        var backtestExchangeGateway = new BacktestExchangeGateway(dataSetService, DATA_SET_NAME);
        var strategy = new SimpleBreakoutStrategy(Interval.ONE_MINUTE, 3, 0.65, 1.3, backtestExchangeGateway);
        var backtestContext = BacktestContext.builder()
            .backtestExchangeGateway(backtestExchangeGateway)
            .strategy(strategy)
            .dataSetName(DATA_SET_NAME)
            .symbol(SYMBOL)
            .fundsPerTrade(FUNDS_PER_TRADE)
            .orderFeePercent(ORDER_FEE_PERCENT)
            .build();
        backtestService.runBacktest(backtestContext);
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
