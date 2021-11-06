package root.tse.domain.backtest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import root.TseApp
import root.tse.domain.strategy_execution.ExchangeGateway
import root.tse.domain.strategy_execution.Interval
import root.tse.domain.strategy_execution.Strategy
import root.tse.domain.strategy_execution.report.EquityCurvePoint
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.rule.RuleCheckResult
import root.tse.domain.strategy_execution.trade.Order
import root.tse.domain.strategy_execution.trade.TradeType
import spock.lang.Specification

import static root.tse.domain.strategy_execution.trade.TradeType.LONG
import static root.tse.util.TestUtils.equalsWithPrecision

@SpringBootTest
@ContextConfiguration(classes = [TseApp])
@Sql('/sql/data-set-1.sql')
class BacktestFunctionalTest extends Specification {

    private static final STRATEGY_ID = '0cedd70a'
    private static final DATA_SET_NAME = 'data_set_1'
    private static final SYMBOL = 'ETH_USD'
    private static final FUNDS_PER_TRADE = 1000d
    private static final TRANSACTION_FEE_PERCENT = 0.2d
    private static final ENTRY_PRICES = [2575.27d, 2457.19d, 2466.14d, 2430.83d, 2395.88d, 2486.89d, 2455.01d, 2450.79d, 2456.55d]
    private static final EXIT_PRICES = [2578.05d, 2481.09d, 2481.33d, 2414.13d, 2451.01d, 2493.58d, 2472.93d, 2473.91d]
    // Trade's profits:
    // -2.922660537 -- timestamp: 1621609800000
    // +5.707104457 -- timestamp: 1621611780000
    // +2.147104382 -- timestamp: 1621612080000
    // -10.85634125 -- timestamp: 1621612500000
    // +18.96431374 -- timestamp: 1621613640000
    // -1.315273293 -- timestamp: 1621614180000
    // +3.284760551 -- timestamp: 1621616520000
    // +5.414825424 -- timestamp: 1621617120000
    // ------------
    // Total profit ~= +20.42383347
    private static final EQUITY_CURVE = [
        new EquityCurvePoint(1621609800000L, -2.9226605365650173d),
        new EquityCurvePoint(1621611780000L, 2.784443920151815d),
        new EquityCurvePoint(1621612080000L, 4.931548301898264d),
        new EquityCurvePoint(1621612500000L, -5.924792947798279d),
        new EquityCurvePoint(1621613640000L, 13.039520790786378d),
        new EquityCurvePoint(1621614180000L, 11.724247497637066d),
        new EquityCurvePoint(1621616520000L, 15.009008048510449d),
        new EquityCurvePoint(1621617120000L, 20.42383347214924d)
    ]

    @Autowired
    private BacktestService backtestService
    @Autowired
    private DataSetService dataSetService

    def 'should perform backtest correctly'() {
        given:
        def backtestExchangeGateway = new BacktestExchangeGateway(dataSetService, DATA_SET_NAME)
        def strategy = new SampleStrategy(backtestExchangeGateway)
        def backtestContext = BacktestContext.builder()
            .backtestExchangeGateway(backtestExchangeGateway)
            .strategy(strategy)
            .dataSetName(DATA_SET_NAME)
            .symbol(SYMBOL)
            .fundsPerTrade(FUNDS_PER_TRADE)
            .transactionFeePercent(TRANSACTION_FEE_PERCENT)
            .build()

        when:
        def backtestReport = backtestService.runBacktest(backtestContext)

        then:
        UUID.fromString(backtestReport.strategyExecutionId)
        backtestReport.symbols == [SYMBOL]
        backtestReport.fundsPerTrade == FUNDS_PER_TRADE
        backtestReport.transactionFeePercent == TRANSACTION_FEE_PERCENT

        and:
        backtestReport.equityCurve == EQUITY_CURVE
        backtestReport.getNTrades() == 9
        backtestReport.getNClosedTrades() == 8
        backtestReport.getNProfitableTrades() == 5
        equalsWithPrecision(backtestReport.getTotalProfit(), 20.42383347d, 8)
    }

    static class SampleStrategy implements Strategy {

        private final ExchangeGateway exchangeGateway

        SampleStrategy(ExchangeGateway exchangeGateway) {
            this.exchangeGateway = exchangeGateway
        }

        @Override
        String getId() { STRATEGY_ID }

        @Override
        String getName() { "strategy-$STRATEGY_ID" }

        @Override
        TradeType getTradeType() { LONG }

        @Override
        EntryRule getEntryRule() {
            new EntryRule() {
                @Override
                RuleCheckResult check(String symbol) {
                    exchangeGateway.getSeries(symbol, Interval.FIVE_MINUTES, 5)
                        .map({series ->
                            def bar = series.getLastBar()
                            ENTRY_PRICES.contains(bar.getClosePrice().doubleValue()) ?
                                RuleCheckResult.satisfied(bar) :
                                RuleCheckResult.notSatisfied()
                        })
                        .orElseGet({ RuleCheckResult.notSatisfied() })
                }
                @Override
                Interval getLowestInterval() { Interval.FIVE_MINUTES }
                @Override
                Interval getHighestInterval() { Interval.FIVE_MINUTES }
            }
        }

        @Override
        ExitRule getExitRule() {
            new ExitRule() {
                @Override
                RuleCheckResult check(Order entryOrder) {
                    def symbol = entryOrder.getSymbol()
                    exchangeGateway.getSeries(symbol, Interval.ONE_MINUTE, 5)
                        .map({series ->
                            def bar = series.getLastBar()
                            EXIT_PRICES.contains(bar.getClosePrice().doubleValue()) ?
                                RuleCheckResult.satisfied(bar) :
                                RuleCheckResult.notSatisfied()
                        })
                        .orElseGet({ RuleCheckResult.notSatisfied() })
                }
                @Override
                Interval getLowestInterval() { Interval.ONE_MINUTE }
            }
        }
    }
}
