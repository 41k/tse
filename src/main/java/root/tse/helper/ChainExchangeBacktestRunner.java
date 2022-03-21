package root.tse.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.backtest.BacktestExchangeGateway;
import root.tse.domain.chain_exchange_execution.ChainExchangeExecution;
import root.tse.domain.chain_exchange_execution.ChainExchangeExecutionContext;
import root.tse.domain.chain_exchange_execution.ChainExchangeService;
import root.tse.domain.event.EventBus;
import root.tse.domain.order.OrderExecutionType;

import java.util.List;

import static root.tse.domain.clock.Interval.ONE_MINUTE;

// Note: backtest.enabled=true should be set in application.yml
//@Component
@RequiredArgsConstructor
public class ChainExchangeBacktestRunner implements CommandLineRunner {

    private static final long START_TIMESTAMP = 1609459260000L;
    private static final long END_TIMESTAMP = 1640679600000L;
    private static final List<String> ASSET_CHAIN = List.of("USD", "LTC", "BTC", "USD");
    private static final double CHAIN_EXCHANGE_AMOUNT = 500d;
    private static final double MIN_PROFIT_THRESHOLD = 0d;

    private final ExchangeGatewayConfigurationProperties exchangeGatewayProperties;
    private final ExchangeGateway exchangeGateway;
    private final ChainExchangeService chainExchangeService;
    private final EventBus eventBus;

    @Override
    public void run(String... args) {
        var backtestExchangeGateway = (BacktestExchangeGateway) exchangeGateway;
        var context = ChainExchangeExecutionContext.builder()
            .assetChain(ASSET_CHAIN)
            .amount(CHAIN_EXCHANGE_AMOUNT)
            .minProfitThreshold(MIN_PROFIT_THRESHOLD)
            .orderExecutionType(OrderExecutionType.STUB)
            .assetCodeDelimiter(exchangeGatewayProperties.getAssetCodeDelimiter())
            .symbolToPrecisionMap(exchangeGatewayProperties.getSymbolToPrecisionMap())
            .nAmountSelectionSteps(exchangeGatewayProperties.getNumberOfAmountSelectionSteps())
            .build();
        var chainExchangeExecution = new ChainExchangeExecution(context, chainExchangeService, eventBus);
        for (var currentTimestamp = START_TIMESTAMP; currentTimestamp <= END_TIMESTAMP; currentTimestamp = currentTimestamp + ONE_MINUTE.inMillis()) {
            backtestExchangeGateway.setCurrentTimestamp(currentTimestamp);
            chainExchangeExecution.run();
        }
    }
}
