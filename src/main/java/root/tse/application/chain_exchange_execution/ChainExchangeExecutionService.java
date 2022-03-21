package root.tse.application.chain_exchange_execution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.chain_exchange_execution.ChainExchangeExecution;
import root.tse.domain.chain_exchange_execution.ChainExchangeExecutionContext;
import root.tse.domain.chain_exchange_execution.ChainExchangeExecutionFactory;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ChainExchangeExecutionService {

    private final ChainExchangeExecutionSettings settings;
    private final ChainExchangeExecutionFactory chainExchangeExecutionFactory;
    private final Map<Integer, ChainExchangeExecution> chainExchangeExecutionStore;

    public void handle(StartChainExchangeExecutionCommand command) {
        var assetChainId = command.getAssetChainId();
        var assetChain = settings.getAssetChain(assetChainId);
        if (chainExchangeExecutionStore.containsKey(assetChainId)) {
            log.warn(">>> chain exchange execution {} is currently executed", assetChain);
            return;
        }
        var context = ChainExchangeExecutionContext.builder()
            .assetChain(assetChain)
            .amount(command.getAmount())
            .minProfitThreshold(command.getMinProfitThreshold())
            .orderExecutionType(command.getOrderExecutionType())
            .assetCodeDelimiter(settings.getAssetCodeDelimiter())
            .symbolToPrecisionMap(settings.getSymbolToPrecisionMap())
            .nAmountSelectionSteps(settings.getNAmountSelectionSteps())
            .build();
        var chainExchangeExecution = chainExchangeExecutionFactory.create(context);
        chainExchangeExecutionStore.put(assetChainId, chainExchangeExecution);
        log.info(">>> chain exchange execution {} has been started", assetChain);
    }

    public void handle(StopChainExchangeExecutionCommand command) {
        var assetChainId = command.getAssetChainId();
        chainExchangeExecutionStore.remove(assetChainId);
        log.info(">>> chain exchange execution {} has been stopped", settings.getAssetChain(assetChainId));
    }
}
