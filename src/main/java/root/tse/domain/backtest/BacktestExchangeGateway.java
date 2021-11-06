package root.tse.domain.backtest;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.ta4j.core.BarSeries;
import root.tse.domain.strategy_execution.ExchangeGateway;
import root.tse.domain.strategy_execution.Interval;
import root.tse.domain.strategy_execution.trade.Order;

import java.util.Optional;

import static root.tse.domain.strategy_execution.trade.OrderStatus.FILLED;

@RequiredArgsConstructor
public class BacktestExchangeGateway implements ExchangeGateway {

    private final DataSetService dataSetService;
    private final String dataSetName;
    @Setter
    private Long currentTimestamp;

    @Override
    public Optional<BarSeries> getSeries(String symbol, Interval interval, Integer seriesLength) {
        var series = dataSetService.getSeries(dataSetName, symbol, interval, currentTimestamp, seriesLength);
        return Optional.of(series);
    }

    @Override
    public Order execute(Order order) {
        return order.toBuilder().status(FILLED).build();
    }

    public Long getStartTimestamp(String symbol) {
        return dataSetService.getStartTimestamp(dataSetName, symbol);
    }

    public Long getEndTimestamp(String symbol) {
        return dataSetService.getEndTimestamp(dataSetName, symbol);
    }
}
