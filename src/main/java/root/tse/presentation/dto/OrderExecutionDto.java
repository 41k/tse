package root.tse.presentation.dto;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.order.OrderType;
import root.tse.domain.order_execution.OrderExecution;

import java.util.Collection;

@Value
@Builder
public class OrderExecutionDto {

    String id;
    OrderType orderType;
    OrderExecutionType orderExecutionType;
    String symbol;
    Double amount;
    Double price;
    Long timestamp;
    Collection<String> ruleDescription;

    public static OrderExecutionDto from(OrderExecution orderExecution) {
        var dtoBuilder = OrderExecutionDto.builder()
            .id(orderExecution.getId())
            .orderType(orderExecution.getContext().getOrderType())
            .orderExecutionType(orderExecution.getContext().getOrderExecutionType())
            .symbol(orderExecution.getContext().getSymbol())
            .amount(orderExecution.getContext().getAmount())
            .ruleDescription(orderExecution.getContext().getRule().getDescription());
        orderExecution.getExecutedOrder().ifPresent(order ->
            dtoBuilder.price(order.getPrice()).timestamp(order.getTimestamp()));
        return dtoBuilder.build();
    }
}
