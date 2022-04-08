package root.tse.application.order_execution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.application.rule.RuleContext;
import root.tse.application.rule.RuleService;
import root.tse.domain.order_execution.OrderExecution;
import root.tse.domain.order_execution.OrderExecutionContext;
import root.tse.domain.order_execution.OrderExecutionFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class OrderExecutionService {

    private final RuleService ruleService;
    private final OrderExecutionFactory orderExecutionFactory;
    private final Map<String, OrderExecution> orderExecutionsStore;

    public void handle(StartOrderExecutionCommand command) {
        var rule = ruleService.buildEntryRule(
            RuleContext.builder()
                .ruleId(command.getRuleId())
                .orderType(command.getOrderType())
                .parameters(command.getRuleParameters())
                .build());
        var orderExecutionContext = OrderExecutionContext.builder()
            .orderExecutionType(command.getOrderExecutionType())
            .orderType(command.getOrderType())
            .symbol(command.getSymbol())
            .amount(command.getAmount())
            .rule(rule)
            .build();
        var orderExecution = orderExecutionFactory.create(orderExecutionContext);
        var orderExecutionId = orderExecution.getId();
        orderExecution.start();
        orderExecutionsStore.put(orderExecutionId, orderExecution);
        log.info(">>> order execution [{}] has been started", orderExecutionId);
    }

    public void handle(StopOrderExecutionCommand command) {
        var orderExecutionId = command.getOrderExecutionId();
        getOrderExecution(orderExecutionId).stop();
        orderExecutionsStore.remove(orderExecutionId);
        log.info(">>> order execution [{}] has been stopped", orderExecutionId);
    }

    public Collection<OrderExecution> getOrderExecutions() {
        return orderExecutionsStore.values();
    }

    private OrderExecution getOrderExecution(String orderExecutionId) {
        return Optional.ofNullable(orderExecutionsStore.get(orderExecutionId))
            .orElseThrow(() -> new IllegalArgumentException("Invalid order execution id"));
    }
}
