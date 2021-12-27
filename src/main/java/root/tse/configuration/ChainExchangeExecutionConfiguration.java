package root.tse.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import root.tse.application.ChainExchangeExecutionService;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.IdGenerator;
import root.tse.domain.chain_exchange_execution.*;
import root.tse.domain.event.EventBus;
import root.tse.domain.order.OrderExecutor;
import root.tse.infrastructure.chain_exchange_execution.ChainExchangeTask;
import root.tse.infrastructure.persistence.chain_exchange.ChainExchangeDbEntryJpaRepository;
import root.tse.infrastructure.persistence.chain_exchange.ChainExchangeRepositoryImpl;
import root.tse.infrastructure.persistence.chain_exchange.ChainExchangeToDbEntryMapper;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(ExchangeGatewayConfigurationProperties.class)
public class ChainExchangeExecutionConfiguration {

    @Bean
    public ChainExchangeExecutionService chainExchangeExecutionService(
        ExchangeGatewayConfigurationProperties exchangeGatewayConfigurationProperties,
        ChainExchangeExecutionFactory chainExchangeExecutionFactory,
        Map<Integer, ChainExchangeExecution> chainExchangeExecutionStore
    ) {
        var chainExchangeExecutionSettings = exchangeGatewayConfigurationProperties.getChainExchangeExecutionSettings();
        return new ChainExchangeExecutionService(
            chainExchangeExecutionSettings, chainExchangeExecutionFactory, chainExchangeExecutionStore);
    }

    @Bean
    public ChainExchangeTask chainExchangeTask(Map<Integer, ChainExchangeExecution> chainExchangeExecutionStore) {
        return new ChainExchangeTask(chainExchangeExecutionStore);
    }

    @Bean
    public Map<Integer, ChainExchangeExecution> chainExchangeExecutionStore() {
        return new HashMap<>();
    }

    @Bean
    public ChainExchangeExecutionFactory chainExchangeExecutionFactory(
        ExchangeGateway exchangeGateway,
        ChainExchangeService chainExchangeService,
        EventBus eventBus
    ) {
        return new ChainExchangeExecutionFactory(exchangeGateway, chainExchangeService, eventBus);
    }

    @Bean
    public ChainExchangeService chainExchangeService(
        IdGenerator idGenerator,
        OrderExecutor orderExecutor,
        ChainExchangeRepository chainExchangeRepository,
        Clock clock
    ) {
        var initialOrderAmountCalculator = new InitialOrderAmountCalculator();
        return new ChainExchangeService(
            idGenerator, orderExecutor, initialOrderAmountCalculator, chainExchangeRepository, clock);
    }

    @Bean
    public ChainExchangeRepository chainExchangeRepository(ChainExchangeDbEntryJpaRepository dbEntryRepository) {
        var mapper = new ChainExchangeToDbEntryMapper();
        return new ChainExchangeRepositoryImpl(mapper, dbEntryRepository);
    }
}
