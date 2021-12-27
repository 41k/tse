package root.tse.domain.chain_exchange_execution;

import lombok.RequiredArgsConstructor;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.event.EventBus;

@RequiredArgsConstructor
public class ChainExchangeExecutionFactory {

    private final ExchangeGateway exchangeGateway;
    private final ChainExchangeService chainExchangeService;
    private final EventBus eventBus;

    public ChainExchangeExecution create(ChainExchangeExecutionContext context) {
        return new ChainExchangeExecution(context, exchangeGateway, chainExchangeService, eventBus);
    }
}
