package root.tse.application.model;

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
    Double orderFeePercent;
    Integer nAmountSelectionSteps;

    public List<String> getAssetChain(Integer id) {
        return Optional.ofNullable(assetChains.get(id)).orElseThrow(() ->
            new NoSuchElementException(String.format(">>> asset chain with id [%s] is not configured", id)));
    }
}
