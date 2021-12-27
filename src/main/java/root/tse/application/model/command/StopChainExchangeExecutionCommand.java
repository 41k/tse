package root.tse.application.model.command;

import lombok.Value;

@Value
public class StopChainExchangeExecutionCommand {
    Integer assetChainId;
}
