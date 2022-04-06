package root.tse.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.IdGenerator;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.event.EventBus;
import root.tse.domain.order_execution.OrderExecutionFactory;

@Configuration
public class OrderExecutionConfiguration {

    @Bean
    public OrderExecutionFactory orderExecutionFactory(
        IdGenerator idGenerator,
        ClockSignalDispatcher clockSignalDispatcher,
        ExchangeGateway exchangeGateway,
        EventBus eventBus
    ) {
        return new OrderExecutionFactory(idGenerator, clockSignalDispatcher, exchangeGateway, eventBus);
    }
}
