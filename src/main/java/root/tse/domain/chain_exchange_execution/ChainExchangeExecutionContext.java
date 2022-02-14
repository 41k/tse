package root.tse.domain.chain_exchange_execution;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.order.OrderExecutionType;

import java.util.List;
import java.util.Map;

@Value
@Builder(toBuilder = true)
public class ChainExchangeExecutionContext {

    List<String> assetChain;
    String assetCodeDelimiter;
    Map<String, Integer> symbolToPrecisionMap;
    Double orderFeePercent;
    Integer nAmountSelectionSteps;
    Double amount;
    Double minProfitThreshold;
    OrderExecutionType orderExecutionType;

    public String getAssetChainAsString() {
        return String.join(assetCodeDelimiter, assetChain);
    }

    public List<String> getSymbols() {
        return List.of(
            assetChain.get(1) + assetCodeDelimiter + assetChain.get(0),
            assetChain.get(1) + assetCodeDelimiter + assetChain.get(2),
            assetChain.get(2) + assetCodeDelimiter + assetChain.get(3)
        );
    }
}
