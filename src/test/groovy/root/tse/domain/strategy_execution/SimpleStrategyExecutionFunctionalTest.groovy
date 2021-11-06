package root.tse.domain.strategy_execution

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration

import static root.tse.domain.strategy_execution.BaseStrategyExecutionFunctionalTest.SampleStrategy
import static root.tse.domain.strategy_execution.StrategyExecutionMode.TRADING
import static root.tse.domain.strategy_execution.trade.OrderStatus.FILLED
import static root.tse.domain.strategy_execution.trade.OrderType.BUY
import static root.tse.domain.strategy_execution.trade.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG

@DirtiesContext
@ContextConfiguration(classes = [TestContextConfiguration])
class SimpleStrategyExecutionFunctionalTest extends BaseStrategyExecutionFunctionalTest {

    @Autowired
    SimpleStrategyExecution strategyExecution

    def setup() {
        strategyExecution.openedTrade = null
    }

    def 'should perform trade successfully'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockSuccessfulEntryOrderExecution(SYMBOL_1)
        mockSuccessfulSeriesRetrievalForExitRule(SYMBOL_1)
        mockSuccessfulExitOrderExecution(SYMBOL_1)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trade opening'
        sleep(2000)

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL)

        and: 'wait for trades closing'
        sleep(2000)

        then:
        def trades = tradeDbEntryJpaRepository.findAll()
        trades.size() == 1

        and:
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).transactionFeePercent == TRANSACTION_FEE_PERCENT
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(0).entryOrderStatus == FILLED
        trades.get(0).exitOrderType == SELL
        trades.get(0).exitOrderAmount == AMOUNT_1
        trades.get(0).exitOrderPrice == PRICE_2
        trades.get(0).exitOrderTimestamp.toEpochMilli() == TIMESTAMP_2
        trades.get(0).exitOrderStatus == FILLED
    }

    def 'should not open trade if clock signal required by entry rule was not dispatched'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal which is not required for entry rule'
        clockSignalDispatcher.dispatch(NOT_REQUIRED_CLOCK_SIGNAL)

        and:
        sleep(2000)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not open trade if series retrieval failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'failed series retrieval for entry rule'
        mockFailedSeriesRetrievalForEntryRule(SYMBOL_1)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trade opening'
        sleep(2000)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not open trade if entry order execution failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockFailedEntryOrderExecution(SYMBOL_1)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trade opening'
        sleep(2000)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not close trade if clock signal required by exit rule was not dispatched'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockSuccessfulEntryOrderExecution(SYMBOL_1)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal which is not required for exit rule'
        clockSignalDispatcher.dispatch(NOT_REQUIRED_CLOCK_SIGNAL)

        and: 'wait for trades closing'
        sleep(2000)

        then: 'trade was created'
        def trades = tradeDbEntryJpaRepository.findAll()
        trades.size() == 1

        and: 'but was not closed'
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).transactionFeePercent == TRANSACTION_FEE_PERCENT
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(0).entryOrderStatus == FILLED
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp
        !trades.get(0).exitOrderStatus
    }

    def 'should not close trade if clock signal has timestamp which is similar to entry order clock signal timestamp'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockSuccessfulEntryOrderExecution(SYMBOL_1)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal with timestamp which is similar to entry order clock signal timestamp'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL_WITH_ENTRY_ORDER_CLOCK_SIGNAL_TIMESTAMP)

        and: 'wait for trades closing'
        sleep(2000)

        then: 'trade was created'
        def trades = tradeDbEntryJpaRepository.findAll()
        trades.size() == 1

        and: 'but was not closed'
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).transactionFeePercent == TRANSACTION_FEE_PERCENT
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(0).entryOrderStatus == FILLED
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp
        !trades.get(0).exitOrderStatus
    }

    def 'should not close trade if series retrieval failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockSuccessfulEntryOrderExecution(SYMBOL_1)
        mockFailedSeriesRetrievalForExitRule(SYMBOL_1)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trade opening'
        sleep(2000)

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL)

        and: 'wait for trades closing'
        sleep(2000)

        then: 'trade was created'
        def trades = tradeDbEntryJpaRepository.findAll()
        trades.size() == 1

        and: 'but was not closed'
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).transactionFeePercent == TRANSACTION_FEE_PERCENT
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(0).entryOrderStatus == FILLED
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp
        !trades.get(0).exitOrderStatus
    }

    def 'should not close trade if exit order execution failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockSuccessfulEntryOrderExecution(SYMBOL_1)
        mockSuccessfulSeriesRetrievalForExitRule(SYMBOL_1)
        mockFailedExitOrderExecution(SYMBOL_1)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trade opening'
        sleep(2000)

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL)

        and: 'wait for trades closing'
        sleep(2000)

        then: 'trade was created'
        def trades = tradeDbEntryJpaRepository.findAll()
        trades.size() == 1

        and: 'but was not closed'
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).transactionFeePercent == TRANSACTION_FEE_PERCENT
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(0).entryOrderStatus == FILLED
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp
        !trades.get(0).exitOrderStatus
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        SimpleStrategyExecution strategyExecution(ExchangeGateway exchangeGateway,
                                                  SimpleStrategyExecutionFactory strategyExecutionFactory) {
            def strategy = new SampleStrategy(exchangeGateway)
            def strategyExecutionContext = StrategyExecutionContext.builder()
                .strategy(strategy)
                .strategyExecutionMode(TRADING)
                .symbols(SYMBOLS)
                .fundsPerTrade(FUNDS_PER_TRADE)
                .transactionFeePercent(TRANSACTION_FEE_PERCENT)
                .build()
            def strategyExecution = strategyExecutionFactory.create(strategyExecutionContext)
            strategyExecution.start()
            strategyExecution
        }
    }
}
