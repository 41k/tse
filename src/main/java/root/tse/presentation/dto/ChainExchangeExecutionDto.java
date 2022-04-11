package root.tse.presentation.dto;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.chain_exchange_execution.ChainExchangeExecution;
import root.tse.domain.order.OrderExecutionType;

import java.util.List;

@Value
@Builder
public class ChainExchangeExecutionDto {

    Integer assetChainId;
    List<String> assetChain;
    OrderExecutionType orderExecutionType;
    Double amount;
    Double minProfitThreshold;

    public static ChainExchangeExecutionDto from(ChainExchangeExecution chainExchangeExecution) {
        var context = chainExchangeExecution.getContext();
        return ChainExchangeExecutionDto.builder()
            .assetChainId(context.getAssetChainId())
            .assetChain(context.getAssetChain())
            .orderExecutionType(context.getOrderExecutionType())
            .amount(context.getAmount())
            .minProfitThreshold(context.getMinProfitThreshold())
            .build();
    }
}
