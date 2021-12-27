package root.tse.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.IdGenerator;
import root.tse.domain.event.EventBus;
import root.tse.domain.event.EventSubscriber;
import root.tse.domain.order.OrderExecutor;
import root.tse.infrastructure.IdGeneratorImpl;

import java.util.List;

@Configuration
public class CommonConfiguration {

    @Bean
    public IdGenerator idGenerator() {
        return new IdGeneratorImpl();
    }

    @Bean
    public OrderExecutor orderExecutor(ExchangeGateway exchangeGateway) {
        return new OrderExecutor(exchangeGateway);
    }

    @Bean
    public EventBus eventBus(List<EventSubscriber> subscribers) {
        return new EventBus(subscribers);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
