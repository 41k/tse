package root.tse.domain;

import org.ta4j.core.BarSeries;
import root.tse.domain.clock.Interval;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

public interface ExchangeGateway {

    Double getOrderFeePercent();

    Optional<BarSeries> getSeries(String symbol, Interval interval, Integer seriesLength);

    Optional<Map<String, Map<OrderType, Double>>> getCurrentPrices(List<String> symbols);

    Optional<Order> tryToExecute(Order order);
}
