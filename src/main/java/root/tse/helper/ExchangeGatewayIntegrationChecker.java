package root.tse.helper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.clock.Interval;
import root.tse.domain.order.Order;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static root.tse.domain.order.OrderExecutionType.MARKET;
import static root.tse.domain.order.OrderType.BUY;
import static root.tse.domain.order.OrderType.SELL;

//@Component
@RequiredArgsConstructor
public class ExchangeGatewayIntegrationChecker implements CommandLineRunner {

    private final ExchangeGateway exchangeGateway;
    private final Clock clock;

    @Override
    public void run(String... args) {
        getCurrentPrice();
//        getSeries();
//        executeOrder();
    }

    @SneakyThrows
    private void getCurrentPrice() {
        while (true) {
            System.out.println("------------------------------------");
            TimeUnit.SECONDS.sleep(5);
            var symbols = List.of("BCH/BTC", "ETH/BTC", "LTC/BTC");
            exchangeGateway.getCurrentPrices(symbols).ifPresent(prices -> prices.entrySet().forEach(entry ->
                System.out.println(entry.getKey() + ": SELL: " + entry.getValue().get(SELL) + " --- BUY: " + entry.getValue().get(BUY))));
        }
    }

    private void getSeries() {
        exchangeGateway.getSeries("ETH/USD", Interval.ONE_DAY, 10).ifPresent(series -> {
            var bars = series.getBarData();
            System.out.println("---------------------------");
            System.out.println("Series length: " + bars.size());
            bars.stream().forEach(bar -> {
                System.out.println("---------------------------");
                System.out.println("Time: " + bar.getSimpleDateName());
                System.out.println("O: " + bar.getOpenPrice().doubleValue());
                System.out.println("H: " + bar.getHighPrice().doubleValue());
                System.out.println("L: " + bar.getLowPrice().doubleValue());
                System.out.println("C: " + bar.getClosePrice().doubleValue());
                System.out.println("V: " + bar.getVolume().doubleValue());
            });
            System.out.println("---------------------------");
        });
    }

    private void executeOrder() {
        var order = Order.builder()
            .type(SELL)
            .executionType(MARKET)
            .symbol("ETH/USD")
            .amount(0.0126605365650173d)
            .price(3000d)
            .timestamp(clock.millis())
            .build();
        var executedOrder = exchangeGateway.tryToExecute(order).get();
        System.out.println("---------------------------");
        System.out.println(">>> " + executedOrder.toString());
        System.out.println("---------------------------");
    }
}
