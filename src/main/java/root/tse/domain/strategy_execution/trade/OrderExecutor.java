package root.tse.domain.strategy_execution.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.strategy_execution.ExchangeGateway;
import root.tse.domain.strategy_execution.StrategyExecutionType;

import static root.tse.domain.strategy_execution.StrategyExecutionType.INCUBATION;
import static root.tse.domain.strategy_execution.trade.OrderStatus.FILLED;

@Slf4j
@RequiredArgsConstructor
public class OrderExecutor {

    private final ExchangeGateway exchangeGateway;

    public Order execute(Order order, StrategyExecutionType executionType) {
        if (INCUBATION.equals(executionType)) {
            return order.toBuilder().status(FILLED).build();
        }
        return exchangeGateway.execute(order);
    }
}
