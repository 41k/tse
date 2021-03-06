package root.tse.domain.chain_exchange_execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.event.EventBus;

@Slf4j
@RequiredArgsConstructor
public class ChainExchangeExecution {

    @Getter
    private final ChainExchangeExecutionContext context;
    private final ChainExchangeService chainExchangeService;
    private final EventBus eventBus;

    public void run() {
        chainExchangeService.tryToFormExpectedChainExchange(context).ifPresentOrElse(
            expectedChainExchange -> {
                var assetChain = context.getAssetChain();
                var chainExchangeId = expectedChainExchange.getId();
                var expectedProfit = expectedChainExchange.getProfit();
                var minProfitThreshold = context.getMinProfitThreshold();
                if (expectedProfit < minProfitThreshold) {
                    log.debug(">>> chain exchange {}:{} will not be executed because expectedProfit[{}] < minProfitThreshold[{}]",
                        chainExchangeId, assetChain, expectedProfit, minProfitThreshold);
                    return;
                }
                log.debug(">>> expected {}", expectedChainExchange);
                chainExchangeService.tryToExecute(expectedChainExchange, context).ifPresentOrElse(
                    executedChainExchange -> eventBus.publishChainExchangeWasExecutedEvent(executedChainExchange, assetChain),
                    () -> eventBus.publishChainExchangeExecutionFailedEvent(chainExchangeId, assetChain));
            },
            () -> log.warn(">>> failed to form expected chain exchange for symbols {}", context.getSymbols())
        );
    }
}
