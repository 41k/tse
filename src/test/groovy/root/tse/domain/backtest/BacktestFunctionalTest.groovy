package root.tse.domain.backtest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import root.tse.BaseFunctionalTest
import root.tse.domain.ExchangeGateway
import root.tse.domain.clock.Interval
import root.tse.domain.order.Order
import root.tse.domain.order.OrderExecutionType
import root.tse.domain.strategy_execution.StrategyExecutionContext
import root.tse.domain.strategy_execution.report.EquityCurvePoint
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.trade.TradeType

import static root.tse.util.TestUtils.equalsWithPrecision

@SpringBootTest(properties = [
    "backtest.enabled=true",
    "backtest.data-set-name=data_set_1"
])
@Sql('/sql/data-set-1.sql')
class BacktestFunctionalTest extends BaseFunctionalTest {

    private static final SYMBOL = 'ETH_USD'
    private static final FUNDS_PER_TRADE = 1000d
    private static final ORDER_FEE_PERCENT = 0.2d
    private static final ENTRY_PRICES = [2550.9d, 2519.52d, 2475.53d, 2466.14d, 2478.96d, 2455.19d, 2450.79d, 2491.35d, 2456.55d]
    private static final EXIT_PRICES = [2558.96d, 2507.19d, 2480.57d, 2491.59d, 2491.21d, 2472.93d, 2502.31d, 2480.57d]
    // Approximate trade's profits:
    // -0.84661024  -- trade time: 1621610100000 - 1621610400000
    // -8.883979398 -- trade time: 1621610700000 - 1621610940000
    // -1.968144679 -- trade time: 1621611900000 - 1621612080000
    // +6.299131694 -- trade time: 1621612200000 - 1621614060000
    // +0.931705296 -- trade time: 1621614300000 - 1621615140000
    // +3.211055448 -- trade time: 1621616400000 - 1621616580000
    // +16.97967881 -- trade time: 1621617300000 - 1621617720000
    // -8.318549376 -- trade time: 1621617900000 - 1621617960000
    // ------------
    // Total profit ~= +7.404531586224039
    private static final EQUITY_CURVE = [
        new EquityCurvePoint(1621610400000L, -0.8466502018895881d),
        new EquityCurvePoint(1621610940000L, -9.73065191650187d),
        new EquityCurvePoint(1621612080000L, -11.698796111886281d),
        new EquityCurvePoint(1621614060000L, -5.399664675714689d),
        new EquityCurvePoint(1621615140000L, -4.467959444488656d),
        new EquityCurvePoint(1621616580000L, -1.2569004225800882d),
        new EquityCurvePoint(1621617720000L, 15.722848964352238d),
        new EquityCurvePoint(1621617960000L, 7.404531586224039d)
    ]

    @Autowired
    private BacktestExchangeGateway exchangeGateway
    @Autowired
    private BacktestService backtestService

    def 'should perform backtest correctly'() {
        given:
        def strategyExecutionContext = StrategyExecutionContext.builder()
            .entryRule(entryRule(exchangeGateway))
            .exitRule(exitRule(exchangeGateway))
            .tradeType(TradeType.LONG)
            .orderExecutionType(OrderExecutionType.STUB)
            .symbols([SYMBOL])
            .fundsPerTrade(FUNDS_PER_TRADE)
            .orderFeePercent(ORDER_FEE_PERCENT)
            .build()

        when:
        def backtestReport = backtestService.runBacktest(strategyExecutionContext)

        then:
        backtestReport.strategyExecutionId
        backtestReport.symbols == [SYMBOL]
        backtestReport.fundsPerTrade == FUNDS_PER_TRADE
        backtestReport.orderFeePercent == ORDER_FEE_PERCENT

        and:
        backtestReport.equityCurve == EQUITY_CURVE
        backtestReport.getNTrades() == 9
        backtestReport.getNClosedTrades() == 8
        backtestReport.getNProfitableTrades() == 4
        equalsWithPrecision(backtestReport.getTotalProfit(), 7.404531586d, 9)
    }

    private EntryRule entryRule(ExchangeGateway exchangeGateway) {
        new EntryRule() {
            @Override
            Interval getCheckInterval() { Interval.FIVE_MINUTES }
            @Override
            boolean isSatisfied(String symbol) {
                exchangeGateway.getSeries(symbol, Interval.FIVE_MINUTES, 5)
                    .map({series -> ENTRY_PRICES.contains(series.getLastBar().getClosePrice().doubleValue()) })
                    .orElse(Boolean.FALSE)
            }
        }
    }

    private ExitRule exitRule(ExchangeGateway exchangeGateway) {
        new ExitRule() {
            @Override
            Interval getCheckInterval() { Interval.ONE_MINUTE }
            @Override
            boolean isSatisfied(Order entryOrder) {
                def symbol = entryOrder.getSymbol()
                exchangeGateway.getSeries(symbol, Interval.ONE_MINUTE, 5)
                    .map({series -> EXIT_PRICES.contains(series.getLastBar().getClosePrice().doubleValue()) })
                    .orElse(Boolean.FALSE)
            }
        }
    }
}
