package root.tse.domain.strategy_execution

import org.springframework.beans.factory.annotation.Autowired

import static root.tse.domain.order.OrderExecutionType.MARKET
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG

class MarketScanningStrategyExecutionFunctionalTest extends BaseStrategyExecutionFunctionalTest {

    @Autowired
    private MarketScanningStrategyExecutionFactory strategyExecutionFactory

    private MarketScanningStrategyExecution strategyExecution

    def setup() {
        exchangeGatewayMock().reset()
        exchangeGatewayMock().currentPrices = [
            (SYMBOL_1) : [(BUY) : PRICE_1, (SELL) : PRICE_2],
            (SYMBOL_2) : [(BUY) : PRICE_2, (SELL) : PRICE_1]
        ]
        exchangeGatewayMock().orderExecutionSuccess = true
        def strategyExecutionContext = StrategyExecutionContext.builder()
            .entryRule(entryRule())
            .exitRule(exitRule())
            .tradeType(LONG)
            .orderExecutionType(MARKET)
            .symbols(SYMBOLS)
            .fundsPerTrade(FUNDS_PER_TRADE)
            .orderFeePercent(ORDER_FEE_PERCENT)
            .marketScanningInterval(ENTRY_RULE_CLOCK_SIGNAL_INTERVAL)
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

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL)

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
        trades.get(0).entryOrderTimestamp.toEpochMilli() == CLOCK_TIMESTAMP
        trades.get(0).exitOrderType == SELL
        trades.get(0).exitOrderAmount == AMOUNT_1
        trades.get(0).exitOrderPrice == PRICE_2
        trades.get(0).exitOrderTimestamp.toEpochMilli() == EXIT_RULE_CLOCK_SIGNAL_TIMESTAMP

        and:
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == CLOCK_TIMESTAMP
        trades.get(1).exitOrderType == SELL
        trades.get(1).exitOrderAmount == AMOUNT_2
        trades.get(1).exitOrderPrice == PRICE_1
        trades.get(1).exitOrderTimestamp.toEpochMilli() == EXIT_RULE_CLOCK_SIGNAL_TIMESTAMP
    }

    def 'should not open trades if clock signal required by entry rule was not dispatched'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal which is not required for entry rule'
        clockSignalDispatcher.dispatch(NOT_REQUIRED_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not open trades if current prices were not obtained'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'no current prices'
        exchangeGatewayMock().currentPrices = null

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not open trades if entry order executions failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'entry orders execution failed'
        exchangeGatewayMock().orderExecutionSuccess = false

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not close trades if clock signal required by exit rule was not dispatched'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal which is not required for exit rule'
        clockSignalDispatcher.dispatch(NOT_REQUIRED_CLOCK_SIGNAL)

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
        trades.get(0).entryOrderTimestamp.toEpochMilli() == CLOCK_TIMESTAMP
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp

        and:
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == CLOCK_TIMESTAMP
        !trades.get(1).exitOrderType
        !trades.get(1).exitOrderAmount
        !trades.get(1).exitOrderPrice
        !trades.get(1).exitOrderTimestamp
    }

    def 'should not close trades if clock signal has timestamp which is less than or equal to entry order timestamp'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal with timestamp which is similar to entry order timestamp'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL_WITH_ENTRY_ORDER_TIMESTAMP)

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
        trades.get(0).entryOrderTimestamp.toEpochMilli() == CLOCK_TIMESTAMP
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp

        and:
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == CLOCK_TIMESTAMP
        !trades.get(1).exitOrderType
        !trades.get(1).exitOrderAmount
        !trades.get(1).exitOrderPrice
        !trades.get(1).exitOrderTimestamp
    }

    def 'should not close trades if exit order executions failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'exit orders execution failed'
        exchangeGatewayMock().orderExecutionSuccess = false

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL)

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
        trades.get(0).entryOrderTimestamp.toEpochMilli() == CLOCK_TIMESTAMP
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp

        and: 'and 2nd was not closed too'
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == CLOCK_TIMESTAMP
        !trades.get(1).exitOrderType
        !trades.get(1).exitOrderAmount
        !trades.get(1).exitOrderPrice
        !trades.get(1).exitOrderTimestamp
    }
}
