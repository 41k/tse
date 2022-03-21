package root.tse.domain.backtest;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.ta4j.core.BarSeries;
import root.tse.configuration.properties.BacktestConfigurationProperties;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.clock.Interval;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class BacktestExchangeGateway implements ExchangeGateway {

    private final BacktestConfigurationProperties backtestProperties;
    private final DataSetService dataSetService;
    @Setter
    private Long currentTimestamp;

    @Override
    public Double getOrderFeePercent() {
        return backtestProperties.getOrderFeePercent();
    }

    @Override
    public Optional<BarSeries> getSeries(String symbol, Interval interval, Integer seriesLength) {
        var dataSetName = backtestProperties.getDataSetName();
        var series = dataSetService.getSeries(dataSetName, symbol, interval, currentTimestamp, seriesLength);
        return Optional.of(series);
    }

    @Override
    public Optional<Map<String, Map<OrderType, Double>>> getCurrentPrices(List<String> symbols) {
        var dataSetName = backtestProperties.getDataSetName();
        return dataSetService.getCurrentPrices(dataSetName, symbols, currentTimestamp);
    }

    @Override
    public Optional<Order> tryToExecute(Order order) {
        var symbol = order.getSymbol();
        var orderType = order.getType();
        return getCurrentPrices(List.of(symbol))
            .map(currentPrices -> currentPrices.get(symbol))
            .map(currentPrices -> currentPrices.get(orderType))
            .map(price -> order.toBuilder().price(price).build());
    }

    public Long getStartTimestamp() {
        var dataSetName = backtestProperties.getDataSetName();
        var symbol = backtestProperties.getSymbol();
        return dataSetService.getStartTimestamp(dataSetName, symbol);
    }

    public Long getEndTimestamp() {
        var dataSetName = backtestProperties.getDataSetName();
        var symbol = backtestProperties.getSymbol();
        return dataSetService.getEndTimestamp(dataSetName, symbol);
    }
}
