package root.tse.domain.order_execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.ClockSignalConsumer;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.clock.Interval;
import root.tse.domain.event.EventBus;
import root.tse.domain.order.Order;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class OrderExecution implements ClockSignalConsumer {

    @Getter
    private final String id;
    @Getter
    private final OrderExecutionContext context;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final ExchangeGateway exchangeGateway;
    private final EventBus eventBus;
    @Getter
    private Optional<Order> executedOrder = Optional.empty();

    public void start() {
        clockSignalDispatcher.subscribe(clockSignalInterval(), this);
    }

    public void stop() {
        clockSignalDispatcher.unsubscribe(clockSignalInterval(), this);
    }

    @Override
    public synchronized void accept(ClockSignal clockSignal) {
        if (executedOrder.isPresent()) {
            return;
        }
        var symbol = context.getSymbol();
        var rule = context.getRule();
        if (rule.isSatisfied(clockSignal, symbol)) {
            var orderType = context.getOrderType();
            var order = Order.builder()
                .type(orderType)
                .executionType(context.getOrderExecutionType())
                .symbol(symbol)
                .amount(context.getAmount())
                .timestamp(clockSignal.getTimestamp())
                .build();
            exchangeGateway.tryToExecute(order)
                .ifPresentOrElse(executedOrder -> {
                    this.executedOrder = Optional.of(executedOrder);
                    stop();
                    eventBus.publishOrderWasExecutedEvent(id, executedOrder);
                },
                () -> eventBus.publishOrderExecutionFailedEvent(id, orderType, symbol));
        }
    }

    private Set<Interval> clockSignalInterval() {
        return Set.of(context.getRule().getCheckInterval());
    }
}
