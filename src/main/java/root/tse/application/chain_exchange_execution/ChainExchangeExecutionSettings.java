package root.tse.application.chain_exchange_execution;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Value
@Builder
public class ChainExchangeExecutionSettings {

    Map<Integer, List<String>> assetChains;
    String assetCodeDelimiter;
    Map<String, Integer> symbolToPrecisionMap;
    Integer nAmountSelectionSteps;

    public List<String> getAssetChain(Integer id) {
        return Optional.ofNullable(assetChains.get(id)).orElseThrow(() ->
            new IllegalArgumentException(String.format("Asset chain with id [%s] is not configured", id)));
    }
}
