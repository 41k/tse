package root.tse.domain.chain_exchange_execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.event.EventBus;

@Slf4j
@RequiredArgsConstructor
public class ChainExchangeExecution {

    @Getter
    private final ChainExchangeExecutionContext context;
    private final ExchangeGateway exchangeGateway;
    private final ChainExchangeService chainExchangeService;
    private final EventBus eventBus;

    public void run() {
        var symbols = context.getSymbols();
        exchangeGateway.getCurrentPrices(symbols).ifPresentOrElse(
            currentPrices -> {
                chainExchangeService.tryToFormExpectedChainExchange(context, currentPrices).ifPresentOrElse(
                    expectedChainExchange -> {
                        var assetChain = context.getAssetChainAsString();
                        var chainExchangeId = expectedChainExchange.getId();
                        var expectedProfit = expectedChainExchange.getProfit();
                        var minProfitThreshold = context.getMinProfitThreshold();
                        if (expectedProfit < minProfitThreshold) {
                            log.debug(">>> chain exchange {}:{} will not be executed because expectedProfit[{}] < minProfitThreshold[{}]",
                                assetChain, chainExchangeId, expectedProfit, minProfitThreshold);
                            return;
                        }
                        log.debug(">>> expected {}", expectedChainExchange);
                        chainExchangeService.tryToExecute(expectedChainExchange, context).ifPresentOrElse(
                            eventBus::publishChainExchangeWasExecutedEvent,
                            () -> eventBus.publishChainExchangeExecutionFailedEvent(chainExchangeId, assetChain));
                    },
                    () -> log.warn(">>> failed to form expected chain exchange for symbols {}", symbols)
                );
            },
            () -> log.warn(">>> failed to retrieve current prices for symbols {}", symbols)
        );
    }
}
