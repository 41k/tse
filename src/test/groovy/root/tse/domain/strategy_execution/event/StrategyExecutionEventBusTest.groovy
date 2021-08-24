package root.tse.domain.strategy_execution.event

import root.tse.domain.strategy_execution.trade.Trade
import spock.lang.Specification

class StrategyExecutionEventBusTest extends Specification {

    private eventSubscriber1 = Mock(StrategyExecutionEventSubscriber)
    private eventSubscriber2 = Mock(StrategyExecutionEventSubscriber)
    private eventBus = new StrategyExecutionEventBus([eventSubscriber1, eventSubscriber2])

    def 'should dispatch events correctly'() {
        given:
        def openedTrade = Trade.builder().build()
        def closedTrade = Trade.builder().build()
        def tradeToClose = Trade.builder().build()
        def strategyExecutionId = 'strategy-execution-id'
        def symbol = 'symbol-1'
        def reason = 'reason-1'

        when:
        eventBus.publishTradeWasOpenedEvent(openedTrade)

        then:
        1 * eventSubscriber1.acceptTradeWasOpenedEvent(openedTrade)
        1 * eventSubscriber2.acceptTradeWasOpenedEvent(openedTrade)
        0 * _

        when:
        eventBus.publishTradeWasNotOpenedEvent(strategyExecutionId, symbol, reason)

        then:
        1 * eventSubscriber1.acceptTradeWasNotOpenedEvent(strategyExecutionId, symbol, reason)
        1 * eventSubscriber2.acceptTradeWasNotOpenedEvent(strategyExecutionId, symbol, reason)
        0 * _

        when:
        eventBus.publishTradeWasClosedEvent(closedTrade)

        then:
        1 * eventSubscriber1.acceptTradeWasClosedEvent(closedTrade)
        1 * eventSubscriber2.acceptTradeWasClosedEvent(closedTrade)
        0 * _

        when:
        eventBus.publishTradeWasNotClosedEvent(tradeToClose, reason)

        then:
        1 * eventSubscriber1.acceptTradeWasNotClosedEvent(tradeToClose, reason)
        1 * eventSubscriber2.acceptTradeWasNotClosedEvent(tradeToClose, reason)
        0 * _
    }
}
