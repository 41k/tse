package root.tse.domain.order_execution;

import lombok.RequiredArgsConstructor;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.IdGenerator;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.event.EventBus;

@RequiredArgsConstructor
public class OrderExecutionFactory {

    private final IdGenerator idGenerator;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final ExchangeGateway exchangeGateway;
    private final EventBus eventBus;

    public OrderExecution create(OrderExecutionContext context) {
        var id = idGenerator.generate();
        return new OrderExecution(id, context, clockSignalDispatcher, exchangeGateway, eventBus);
    }
}
