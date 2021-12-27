package root.tse.domain.strategy_execution

import org.springframework.beans.factory.annotation.Autowired

import static root.tse.domain.order.OrderExecutionMode.EXCHANGE_GATEWAY
import static root.tse.domain.order.OrderStatus.FILLED
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG

class MarketScanningStrategyExecutionFunctionalTest extends BaseStrategyExecutionFunctionalTest {

    @Autowired
    private MarketScanningStrategyExecutionFactory strategyExecutionFactory

    private MarketScanningStrategyExecution strategyExecution

    def setup() {
        def strategy = new SampleStrategy(exchangeGateway)
        def strategyExecutionContext = StrategyExecutionContext.builder()
            .strategy(strategy)
            .orderExecutionMode(EXCHANGE_GATEWAY)
            .symbols(SYMBOLS)
            .fundsPerTrade(FUNDS_PER_TRADE)
            .orderFeePercent(ORDER_FEE_PERCENT)
            .allowedNumberOfSimultaneouslyOpenedTrades(NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES)
            .build()
        strategyExecution = strategyExecutionFactory.create(strategyExecutionContext)
        strategyExecution.start()
    }

    def cleanup() {
        strategyExecution.stop()
    }

    def 'should trade for each scanned symbol'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps for SYMBOL_1'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockSuccessfulEntryOrderExecution(SYMBOL_1)
        mockSuccessfulSeriesRetrievalForExitRule(SYMBOL_1)
        mockSuccessfulExitOrderExecution(SYMBOL_1)

        and: 'mock trade execution steps for SYMBOL_2'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_2)
        mockSuccessfulEntryOrderExecution(SYMBOL_2)
        mockSuccessfulSeriesRetrievalForExitRule(SYMBOL_2)
        mockSuccessfulExitOrderExecution(SYMBOL_2)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL)

        and: 'wait for trades closing'
        sleep(2000)

        then:
        def trades = tradeDbEntryJpaRepository.findAll().sort({it.symbol})
        trades.size() == 2

        and:
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
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

        and:
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(1).entryOrderStatus == FILLED
        trades.get(1).exitOrderType == SELL
        trades.get(1).exitOrderAmount == AMOUNT_2
        trades.get(1).exitOrderPrice == PRICE_1
        trades.get(1).exitOrderTimestamp.toEpochMilli() == TIMESTAMP_2
        trades.get(1).exitOrderStatus == FILLED
    }

    def 'should not open trades if clock signal required by entry rule was not dispatched'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal which is not required for entry rule'
        clockSignalDispatcher.dispatch(NOT_REQUIRED_CLOCK_SIGNAL)

        and:
        sleep(4000)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not open trades if series retrieval failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'failed series retrieval for entry rule with SYMBOL_1'
        mockFailedSeriesRetrievalForEntryRule(SYMBOL_1)

        and: 'failed series retrieval for entry rule with SYMBOL_2'
        mockFailedSeriesRetrievalForEntryRule(SYMBOL_2)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(4000)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not open trades if entry order executions failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps for SYMBOL_1'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockFailedEntryOrderExecution(SYMBOL_1)

        and: 'mock trade execution steps for SYMBOL_2'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_2)
        mockFailedEntryOrderExecution(SYMBOL_2)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(4000)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not close trades if clock signal required by exit rule was not dispatched'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps for SYMBOL_1'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockSuccessfulEntryOrderExecution(SYMBOL_1)

        and: 'mock trade execution steps for SYMBOL_2'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_2)
        mockSuccessfulEntryOrderExecution(SYMBOL_2)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal which is not required for exit rule'
        clockSignalDispatcher.dispatch(NOT_REQUIRED_CLOCK_SIGNAL)

        and: 'wait for trades closing'
        sleep(2000)

        then:
        def trades = tradeDbEntryJpaRepository.findAll().sort({it.symbol})
        trades.size() == 2

        and:
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
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

        and:
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(1).entryOrderStatus == FILLED
        !trades.get(1).exitOrderType
        !trades.get(1).exitOrderAmount
        !trades.get(1).exitOrderPrice
        !trades.get(1).exitOrderTimestamp
        !trades.get(1).exitOrderStatus
    }

    def 'should not close trades if clock signal has timestamp which is similar to entry order clock signal timestamp'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps for SYMBOL_1'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockSuccessfulEntryOrderExecution(SYMBOL_1)

        and: 'mock trade execution steps for SYMBOL_2'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_2)
        mockSuccessfulEntryOrderExecution(SYMBOL_2)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal with timestamp which is similar to entry order clock signal timestamp'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL_WITH_ENTRY_ORDER_CLOCK_SIGNAL_TIMESTAMP)

        and: 'wait for trades closing'
        sleep(2000)

        then:
        def trades = tradeDbEntryJpaRepository.findAll().sort({it.symbol})
        trades.size() == 2

        and:
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
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

        and:
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(1).entryOrderStatus == FILLED
        !trades.get(1).exitOrderType
        !trades.get(1).exitOrderAmount
        !trades.get(1).exitOrderPrice
        !trades.get(1).exitOrderTimestamp
        !trades.get(1).exitOrderStatus
    }

    def 'should not close trades if series retrieval failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps for SYMBOL_1'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockSuccessfulEntryOrderExecution(SYMBOL_1)
        mockFailedSeriesRetrievalForExitRule(SYMBOL_1)

        and: 'mock trade execution steps for SYMBOL_2'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_2)
        mockSuccessfulEntryOrderExecution(SYMBOL_2)
        mockFailedSeriesRetrievalForExitRule(SYMBOL_2)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL)

        and: 'wait for trades closing'
        sleep(2000)

        then: 'trades were created'
        def trades = tradeDbEntryJpaRepository.findAll().sort({it.symbol})
        trades.size() == 2

        and: 'but 1st trade was not closed'
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
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

        and: 'and 2nd was not closed too'
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(1).entryOrderStatus == FILLED
        !trades.get(1).exitOrderType
        !trades.get(1).exitOrderAmount
        !trades.get(1).exitOrderPrice
        !trades.get(1).exitOrderTimestamp
        !trades.get(1).exitOrderStatus
    }

    def 'should not close trades if exit order executions failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'mock trade execution steps for SYMBOL_1'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_1)
        mockSuccessfulEntryOrderExecution(SYMBOL_1)
        mockSuccessfulSeriesRetrievalForExitRule(SYMBOL_1)
        mockFailedExitOrderExecution(SYMBOL_1)

        and: 'mock trade execution steps for SYMBOL_2'
        mockSuccessfulSeriesRetrievalForEntryRule(SYMBOL_2)
        mockSuccessfulEntryOrderExecution(SYMBOL_2)
        mockSuccessfulSeriesRetrievalForExitRule(SYMBOL_2)
        mockFailedExitOrderExecution(SYMBOL_2)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL)

        and: 'wait for trades closing'
        sleep(2000)

        then: 'trades were created'
        def trades = tradeDbEntryJpaRepository.findAll().sort({it.symbol})
        trades.size() == 2

        and: 'but 1st trade was not closed'
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
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

        and: 'and 2nd was not closed too'
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(1).entryOrderStatus == FILLED
        !trades.get(1).exitOrderType
        !trades.get(1).exitOrderAmount
        !trades.get(1).exitOrderPrice
        !trades.get(1).exitOrderTimestamp
        !trades.get(1).exitOrderStatus
    }
}
