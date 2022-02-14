package root.tse.domain.strategy_execution

import org.springframework.beans.factory.annotation.Autowired

import static root.tse.domain.order.OrderExecutionType.MARKET
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG

class SimpleStrategyExecutionFunctionalTest extends BaseStrategyExecutionFunctionalTest {

    @Autowired
    private SimpleStrategyExecutionFactory strategyExecutionFactory

    private SimpleStrategyExecution strategyExecution

    def setup() {
        exchangeGatewayMock().reset()
        exchangeGatewayMock().currentPrices = [(SYMBOL_1) : [(BUY) : PRICE_1, (SELL) : PRICE_2]]
        exchangeGatewayMock().orderExecutionSuccess = true
        def strategyExecutionContext = StrategyExecutionContext.builder()
            .entryRule(entryRule())
            .exitRule(exitRule())
            .tradeType(LONG)
            .orderExecutionType(MARKET)
            .symbols([SYMBOL_1])
            .fundsPerTrade(FUNDS_PER_TRADE)
            .orderFeePercent(ORDER_FEE_PERCENT)
            .build()
        strategyExecution = strategyExecutionFactory.create(strategyExecutionContext)
        strategyExecution.start()
    }

    def cleanup() {
        strategyExecution.stop()
    }

    def 'should perform trade successfully'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL)

        then:
        def trades = tradeDbEntryJpaRepository.findAll()
        trades.size() == 1

        and:
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == ENTRY_RULE_CLOCK_SIGNAL_TIMESTAMP
        trades.get(0).exitOrderType == SELL
        trades.get(0).exitOrderAmount == AMOUNT_1
        trades.get(0).exitOrderPrice == PRICE_2
        trades.get(0).exitOrderTimestamp.toEpochMilli() == EXIT_RULE_CLOCK_SIGNAL_TIMESTAMP
    }

    def 'should not open trade if clock signal required by entry rule was not dispatched'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal which is not required for entry rule'
        clockSignalDispatcher.dispatch(NOT_REQUIRED_CLOCK_SIGNAL)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not open trade if current price was not obtained'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'no current prices'
        exchangeGatewayMock().currentPrices = null

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not open trade if entry order execution failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'entry order execution failed'
        exchangeGatewayMock().orderExecutionSuccess = false

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not close trade if clock signal required by exit rule was not dispatched'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'clock signal which is not required for exit rule'
        clockSignalDispatcher.dispatch(NOT_REQUIRED_CLOCK_SIGNAL)

        then: 'trade was created'
        def trades = tradeDbEntryJpaRepository.findAll()
        trades.size() == 1

        and: 'but was not closed'
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == ENTRY_RULE_CLOCK_SIGNAL_TIMESTAMP
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp
    }

    def 'should not close trade if clock signal has timestamp which is less than or equal to entry order timestamp'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'clock signal with timestamp which is similar to entry order timestamp'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL_WITH_ENTRY_ORDER_TIMESTAMP)

        then: 'trade was created'
        def trades = tradeDbEntryJpaRepository.findAll()
        trades.size() == 1

        and: 'but was not closed'
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == ENTRY_RULE_CLOCK_SIGNAL_TIMESTAMP
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp
    }

    def 'should not close trade if exit order execution failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(ENTRY_RULE_CLOCK_SIGNAL)

        and: 'exit order execution failed'
        exchangeGatewayMock().orderExecutionSuccess = false

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(EXIT_RULE_CLOCK_SIGNAL)

        then: 'trade was created'
        def trades = tradeDbEntryJpaRepository.findAll()
        trades.size() == 1

        and: 'but was not closed'
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).orderFeePercent == ORDER_FEE_PERCENT
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == ENTRY_RULE_CLOCK_SIGNAL_TIMESTAMP
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp
    }
}
