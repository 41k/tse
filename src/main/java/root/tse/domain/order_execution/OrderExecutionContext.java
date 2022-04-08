package root.tse.domain.order_execution;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.order.OrderType;
import root.tse.domain.rule.EntryRule;

@Value
@Builder
public class OrderExecutionContext {
    OrderExecutionType orderExecutionType;
    OrderType orderType;
    String symbol;
    Double amount;
    EntryRule rule;
}
