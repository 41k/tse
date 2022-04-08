package root.tse.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import root.tse.application.order_execution.OrderExecutionService;
import root.tse.application.rule.RuleService;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.IdGenerator;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.event.EventBus;
import root.tse.domain.order_execution.OrderExecution;
import root.tse.domain.order_execution.OrderExecutionFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OrderExecutionConfiguration {

    @Bean
    public OrderExecutionService orderExecutionService(
        RuleService ruleService,
        OrderExecutionFactory orderExecutionFactory,
        Map<String, OrderExecution> orderExecutionsStore
    ) {
        return new OrderExecutionService(ruleService, orderExecutionFactory, orderExecutionsStore);
    }

    @Bean
    public OrderExecutionFactory orderExecutionFactory(
        IdGenerator idGenerator,
        ClockSignalDispatcher clockSignalDispatcher,
        ExchangeGateway exchangeGateway,
        EventBus eventBus
    ) {
        return new OrderExecutionFactory(idGenerator, clockSignalDispatcher, exchangeGateway, eventBus);
    }

    @Bean
    public Map<String, OrderExecution> orderExecutionsStore() {
        return new HashMap<>();
    }
}
