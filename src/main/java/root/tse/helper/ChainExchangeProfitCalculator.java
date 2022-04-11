package root.tse.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.ta4j.core.BarSeries;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.IdGenerator;
import root.tse.domain.chain_exchange_execution.ChainExchangeExecutionContext;
import root.tse.domain.chain_exchange_execution.ChainExchangeRepository;
import root.tse.domain.chain_exchange_execution.ChainExchangeService;
import root.tse.domain.chain_exchange_execution.InitialOrderAmountCalculator;
import root.tse.domain.clock.Interval;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.order.OrderType;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static root.tse.domain.order.OrderType.BUY;
import static root.tse.domain.order.OrderType.SELL;

//@Component
@RequiredArgsConstructor
public class ChainExchangeProfitCalculator implements CommandLineRunner {

    private final ExchangeGatewayConfigurationProperties exchangeGatewayConfigurationProperties;
    private final IdGenerator idGenerator;
    private final ChainExchangeRepository chainExchangeRepository;
    private final Clock clock;

    @Override
    public void run(String... args) {
        var exchangeGateway = buildExchangeGatewayProvidingPrices(Map.of(
            "LTC/USD", Map.of(BUY, 118.3d),
            "LTC/BTC", Map.of(SELL, 0.00291d),
            "BTC/USD", Map.of(SELL, 41169.25d)
        ));
        var initialOrderAmountCalculator = new InitialOrderAmountCalculator();
        var chainExchangeService = new ChainExchangeService(idGenerator, exchangeGateway, initialOrderAmountCalculator, chainExchangeRepository, clock);
        var context = ChainExchangeExecutionContext.builder()
            .assetChainId(1)
            .assetChain(List.of("USD", "LTC", "BTC", "USD"))
            .amount(50d)
            .minProfitThreshold(-10d)
            .orderExecutionType(OrderExecutionType.STUB)
            .assetCodeDelimiter(exchangeGatewayConfigurationProperties.getAssetCodeDelimiter())
            .symbolToPrecisionMap(exchangeGatewayConfigurationProperties.getSymbolToPrecisionMap())
            .nAmountSelectionSteps(exchangeGatewayConfigurationProperties.getNumberOfAmountSelectionSteps())
            .build();
        var expectedChainExchange = chainExchangeService.tryToFormExpectedChainExchange(context).get();
        System.out.println("------------------------------------------");
        System.out.println(">>> expected profit: " + expectedChainExchange.getProfit());
        System.out.println("------------------------------------------");
    }

    private ExchangeGateway buildExchangeGatewayProvidingPrices(Map<String, Map<OrderType, Double>> prices) {
        return new ExchangeGateway() {
            @Override
            public Double getOrderFeePercent() { return exchangeGatewayConfigurationProperties.getOrderFeePercent(); }
            @Override
            public Optional<BarSeries> getSeries(String symbol, Interval interval, Integer seriesLength) { return Optional.empty(); }
            @Override
            public Optional<Map<String, Map<OrderType, Double>>> getCurrentPrices(List<String> symbols) { return Optional.of(prices); }
            @Override
            public Optional<Order> tryToExecute(Order order) { return Optional.empty(); }
        };
    }
}
