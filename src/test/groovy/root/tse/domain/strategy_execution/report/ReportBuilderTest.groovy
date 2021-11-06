package root.tse.domain.strategy_execution.report

import root.tse.domain.strategy_execution.StrategyExecution
import root.tse.domain.strategy_execution.StrategyExecutionContext
import root.tse.domain.strategy_execution.trade.Order
import root.tse.domain.strategy_execution.trade.Trade
import root.tse.domain.strategy_execution.trade.TradeRepository
import root.tse.domain.strategy_execution.trade.TradeType
import spock.lang.Specification

import static root.tse.util.TestUtils.*

class ReportBuilderTest extends Specification {

    private static final TIMESTAMP_1 = 1639530000000L
    private static final TIMESTAMP_2 = 1639533600000L
    private static final TIMESTAMP_3 = 1639537200000L
    private static final TIMESTAMP_4 = 1639540800000L

    private static final EQUITY_CURVE = [
        new EquityCurvePoint(TIMESTAMP_1, 1.9267980000000016d),
        new EquityCurvePoint(TIMESTAMP_2, -0.7059775999999927d),
        new EquityCurvePoint(TIMESTAMP_3, -1.954077799999964d),
        new EquityCurvePoint(TIMESTAMP_4, 0.7757982000000814d)
    ]

    private strategyExecutionContext = StrategyExecutionContext.builder()
        .symbols(SYMBOLS).fundsPerTrade(FUNDS_PER_TRADE).transactionFeePercent(TRANSACTION_FEE_PERCENT).build()

    private trade1 = tradeBuilder() // profit = 1.926798
        .entryOrder(Order.builder().price(2475.89d).amount(0.1d).build())
        .exitOrder(Order.builder().price(2505.12d).amount(0.1d).timestamp(TIMESTAMP_1).build()).build()
    private trade2 = tradeBuilder() // profit = -2.6327756
        .entryOrder(Order.builder().price(3080.44d).amount(0.07d).build())
        .exitOrder(Order.builder().price(3055.1d).amount(0.07d).timestamp(TIMESTAMP_2).build()).build()
    private trade3 = tradeBuilder() // profit = -1.2481002
        .entryOrder(Order.builder().price(4003.37d).amount(0.09d).build())
        .exitOrder(Order.builder().price(4005.52d).amount(0.09d).timestamp(TIMESTAMP_3).build()).build()
    private trade4 = tradeBuilder() // profit = ...
        .entryOrder(Order.builder().price(3802.67d).amount(0.1d).build()).build()
    private trade5 = tradeBuilder() // profit = 2.729876
        .entryOrder(Order.builder().price(4010.28d).amount(0.2d).build())
        .exitOrder(Order.builder().price(4040.03d).amount(0.2d).timestamp(TIMESTAMP_4).build()).build()

    private strategyExecution = Mock(StrategyExecution)
    private tradeRepository = Mock(TradeRepository)

    private reportBuilder = new ReportBuilder(tradeRepository)

    def 'should build report correctly'() {
        when:
        def report = reportBuilder.build(strategyExecution)

        then:
        1 * strategyExecution.getId() >> STRATEGY_EXECUTION_ID
        1 * strategyExecution.getContext() >> strategyExecutionContext
        1 * tradeRepository.getAllTradesByStrategyExecutionId(STRATEGY_EXECUTION_ID) >> [trade1, trade2, trade3, trade4, trade5]
        0 * _

        and:
        report.strategyExecutionId == STRATEGY_EXECUTION_ID
        report.symbols == SYMBOLS
        report.fundsPerTrade == FUNDS_PER_TRADE
        report.transactionFeePercent == TRANSACTION_FEE_PERCENT

        and:
        report.equityCurve == EQUITY_CURVE
        report.getNTrades() == 5
        report.getNClosedTrades() == 4
        report.getNProfitableTrades() == 2
        report.getTotalProfit() == 0.7757982000000814d
    }

    private Trade.TradeBuilder tradeBuilder() {
        Trade.builder().id(TRADE_ID).strategyExecutionId(STRATEGY_EXECUTION_ID)
            .type(TradeType.LONG).transactionFeePercent(TRANSACTION_FEE_PERCENT)
    }
}
