package root.tse.application.order_execution

import root.tse.application.rule.RuleContext
import root.tse.application.rule.RuleService
import root.tse.domain.order_execution.OrderExecution
import root.tse.domain.order_execution.OrderExecutionContext
import root.tse.domain.order_execution.OrderExecutionFactory
import root.tse.domain.rule.EntryRule
import spock.lang.Specification

import static root.tse.domain.order.OrderType.BUY
import static root.tse.util.TestUtils.*

class OrderExecutionServiceTest extends Specification {

    private static final RULE_PARAMETERS = [:] as Map
    private static final START_ORDER_EXECUTION_COMMAND = StartOrderExecutionCommand.builder()
        .orderExecutionType(ORDER_EXECUTION_TYPE).orderType(BUY).symbol(SYMBOL_1)
        .amount(AMOUNT_1).ruleId(ENTRY_RULE_ID).ruleParameters(RULE_PARAMETERS).build()
    private static final RULE_CONTEXT = RuleContext.builder()
        .ruleId(ENTRY_RULE_ID).orderType(BUY).parameters(RULE_PARAMETERS).build()

    private rule = Mock(EntryRule)
    private ruleService = Mock(RuleService)
    private orderExecutionFactory = Mock(OrderExecutionFactory)
    private orderExecution = Mock(OrderExecution)

    private Map<String, OrderExecution> orderExecutionsStore
    private OrderExecutionService orderExecutionService

    def setup() {
        orderExecutionsStore = [:]
        orderExecutionService = new OrderExecutionService(
            ruleService, orderExecutionFactory, orderExecutionsStore)
    }

    def 'should start order execution successfully'() {
        given: 'no active order executions'
        assert orderExecutionsStore.isEmpty()

        and:
        def orderExecutionContext = OrderExecutionContext.builder()
            .orderExecutionType(ORDER_EXECUTION_TYPE)
            .orderType(BUY)
            .symbol(SYMBOL_1)
            .amount(AMOUNT_1)
            .rule(rule)
            .build()

        when:
        orderExecutionService.handle(START_ORDER_EXECUTION_COMMAND)

        then:
        1 * ruleService.buildEntryRule(RULE_CONTEXT) >> rule
        1 * orderExecutionFactory.create(orderExecutionContext) >> orderExecution
        1 * orderExecution.getId() >> ORDER_EXECUTION_ID
        1 * orderExecution.start()
        0 * _

        and:
        orderExecutionsStore.size() == 1
        orderExecutionsStore.get(ORDER_EXECUTION_ID) == orderExecution
    }

    def 'should stop order execution successfully'() {
        given: 'active order execution'
        orderExecutionsStore.put(ORDER_EXECUTION_ID, orderExecution)

        and:
        def command = new StopOrderExecutionCommand(ORDER_EXECUTION_ID)

        when:
        orderExecutionService.handle(command)

        then: 'order execution was stopped'
        1 * orderExecution.stop()
        0 * _

        and: 'order execution was removed from store'
        !orderExecutionsStore.get(ORDER_EXECUTION_ID)
    }

    def 'should not stop order execution if order execution id is invalid'() {
        given: 'active order execution'
        orderExecutionsStore.put(ORDER_EXECUTION_ID, orderExecution)

        and:
        def command = new StopOrderExecutionCommand('invalid-order-execution-id')

        when:
        orderExecutionService.handle(command)

        then:
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid order execution id'

        and: 'order execution was not stopped'
        orderExecutionsStore.get(ORDER_EXECUTION_ID) == orderExecution
    }

    def 'should provide order executions'() {
        given:
        def orderExecution1 = Mock(OrderExecution)
        def orderExecution2 = Mock(OrderExecution)
        orderExecutionsStore.put('1', orderExecution1)
        orderExecutionsStore.put('2', orderExecution2)
        assert orderExecutionsStore.size() == 2

        expect:
        orderExecutionService.getOrderExecutions() as List == [orderExecution1, orderExecution2]
    }
}
