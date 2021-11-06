package root.tse.domain.backtest

import root.tse.domain.strategy_execution.Strategy
import root.tse.domain.strategy_execution.StrategyExecutionContext
import spock.lang.Specification

import static root.tse.domain.strategy_execution.StrategyExecutionMode.INCUBATION
import static root.tse.util.TestUtils.*

class BacktestContextTest extends Specification {

    private backtestExchangeGateway = Mock(BacktestExchangeGateway)
    private strategy = Mock(Strategy)
    private context = BacktestContext.builder()
        .backtestExchangeGateway(backtestExchangeGateway)
        .strategy(strategy)
        .dataSetName(DATA_SET_NAME)
        .symbol(SYMBOL_1)
        .fundsPerTrade(FUNDS_PER_TRADE)
        .transactionFeePercent(TRANSACTION_FEE_PERCENT)
        .build()

    def 'should provide data which was set'() {
        expect:
        context.backtestExchangeGateway == backtestExchangeGateway
        context.strategy == strategy
        context.dataSetName == DATA_SET_NAME
        context.symbol == SYMBOL_1
        context.fundsPerTrade == FUNDS_PER_TRADE
        context.transactionFeePercent == TRANSACTION_FEE_PERCENT
    }

    def 'should provide proper strategy execution context'() {
        expect:
        context.getStrategyExecutionContext() == StrategyExecutionContext.builder()
            .strategy(strategy)
            .strategyExecutionMode(INCUBATION)
            .symbols([SYMBOL_1])
            .fundsPerTrade(FUNDS_PER_TRADE)
            .transactionFeePercent(TRANSACTION_FEE_PERCENT)
            .build()
    }

    def 'should provide start timestamp'() {
        when:
        def startTimestamp = context.getStartTimestamp()

        then:
        1 * backtestExchangeGateway.getStartTimestamp(SYMBOL_1) >> TIMESTAMP_1
        0 * _

        and:
        startTimestamp == TIMESTAMP_1
    }

    def 'should provide end timestamp'() {
        when:
        def endTimestamp = context.getEndTimestamp()

        then:
        1 * backtestExchangeGateway.getEndTimestamp(SYMBOL_1) >> TIMESTAMP_2
        0 * _

        and:
        endTimestamp == TIMESTAMP_2
    }
}
