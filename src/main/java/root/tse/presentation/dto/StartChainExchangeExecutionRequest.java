package root.tse.presentation.dto;

import lombok.Data;
import root.tse.domain.order.OrderExecutionType;

import javax.validation.constraints.NotNull;

@Data
public class StartChainExchangeExecutionRequest {
    @NotNull
    private Integer assetChainId;
    @NotNull
    private OrderExecutionType orderExecutionType;
    @NotNull
    private Double amount;
    @NotNull
    private Double minProfitThreshold;
}
