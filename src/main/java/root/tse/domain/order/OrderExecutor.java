package root.tse.domain.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.ExchangeGateway;

import static root.tse.domain.order.OrderExecutionMode.STUB;
import static root.tse.domain.order.OrderStatus.FILLED;

@Slf4j
@RequiredArgsConstructor
public class OrderExecutor {

    private final ExchangeGateway exchangeGateway;

    public Order execute(Order order, OrderExecutionMode executionMode) {
        if (STUB.equals(executionMode)) {
            return order.toBuilder().status(FILLED).build();
        }
        return exchangeGateway.execute(order);
    }
}
