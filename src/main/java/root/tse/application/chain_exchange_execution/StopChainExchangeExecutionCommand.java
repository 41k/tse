package root.tse.application.chain_exchange_execution;

import lombok.Value;

@Value
public class StopChainExchangeExecutionCommand {
    Integer assetChainId;
}
