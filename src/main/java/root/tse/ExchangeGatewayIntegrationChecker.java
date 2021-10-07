package root.tse;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import root.tse.domain.strategy_execution.ExchangeGateway;
import root.tse.domain.strategy_execution.Interval;
import root.tse.domain.strategy_execution.trade.Order;
import root.tse.domain.strategy_execution.trade.OrderType;

//@Component
@RequiredArgsConstructor
public class ExchangeGatewayIntegrationChecker implements CommandLineRunner {

    private final ExchangeGateway exchangeGateway;

    @Override
    public void run(String... args) {
        getSeries();
//        executeOrder();
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
            .type(OrderType.SELL)
            .symbol("ETH/USD")
            .amount(0.01d)
            .price(3000d)
            .build();
        var executedOrder = exchangeGateway.execute(order);
        System.out.println("---------------------------");
        System.out.println(">>> " + executedOrder.toString());
        System.out.println("---------------------------");
    }
}
