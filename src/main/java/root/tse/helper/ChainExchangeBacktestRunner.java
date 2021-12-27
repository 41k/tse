package root.tse.helper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;
import root.tse.domain.backtest.BacktestContext;
import root.tse.domain.backtest.BacktestExchangeGateway;
import root.tse.domain.backtest.BacktestService;
import root.tse.domain.backtest.DataSetService;
import root.tse.domain.chain_exchange_execution.ChainExchangeExecution;
import root.tse.domain.chain_exchange_execution.ChainExchangeExecutionContext;
import root.tse.domain.chain_exchange_execution.ChainExchangeService;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.Interval;
import root.tse.domain.event.EventBus;
import root.tse.domain.order.OrderExecutionMode;
import root.tse.domain.strategy.SimpleBreakoutStrategy;

import java.util.List;

import static root.tse.domain.clock.Interval.ONE_MINUTE;

//@Component
@RequiredArgsConstructor
public class ChainExchangeBacktestRunner implements CommandLineRunner {

    private static final String DATA_SET_NAME = "bitstamp_1m_data_set";
    private static final long START_TIMESTAMP = 1609459260000L;
    private static final long END_TIMESTAMP = 1640679600000L;
    private static final List<String> ASSET_CHAIN = List.of("USD", "LTC", "BTC", "USD");
    private static final double CHAIN_EXCHANGE_AMOUNT = 500d;
    private static final double MIN_PROFIT_THRESHOLD = 0d;

    private final ExchangeGatewayConfigurationProperties exchangeGatewayConfigurationProperties;
    private final DataSetService dataSetService;
    private final ChainExchangeService chainExchangeService;
    private final EventBus eventBus;

    @Override
    public void run(String... args) {
        var context = ChainExchangeExecutionContext.builder()
            .assetChain(ASSET_CHAIN)
            .amount(CHAIN_EXCHANGE_AMOUNT)
            .minProfitThreshold(MIN_PROFIT_THRESHOLD)
            .orderExecutionMode(OrderExecutionMode.STUB)
            .assetCodeDelimiter(exchangeGatewayConfigurationProperties.getAssetCodeDelimiter())
            .symbolToPrecisionMap(exchangeGatewayConfigurationProperties.getSymbolToPrecisionMap())
            .orderFeePercent(exchangeGatewayConfigurationProperties.getOrderFeePercent())
            .nAmountSelectionSteps(exchangeGatewayConfigurationProperties.getNumberOfAmountSelectionSteps())
            .build();
        var exchangeGateway = new BacktestExchangeGateway(dataSetService, DATA_SET_NAME);
        var chainExchangeExecution = new ChainExchangeExecution(context, exchangeGateway, chainExchangeService, eventBus);
        for (var currentTimestamp = START_TIMESTAMP; currentTimestamp <= END_TIMESTAMP; currentTimestamp = currentTimestamp + ONE_MINUTE.inMillis()) {
            exchangeGateway.setCurrentTimestamp(currentTimestamp);
            chainExchangeExecution.run();
        }
    }
}
