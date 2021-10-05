package root.tse.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import root.tse.domain.strategy_execution.ExchangeGateway;
import root.tse.domain.strategy_execution.StrategyExecutionFactory;
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus;
import root.tse.domain.strategy_execution.trade.OrderExecutor;
import root.tse.domain.strategy_execution.trade.TradeExecutionFactory;
import root.tse.domain.strategy_execution.trade.TradeRepository;
import root.tse.infrastructure.persistence.trade.TradeDbEntryJpaRepository;
import root.tse.infrastructure.persistence.trade.TradeRepositoryImpl;
import root.tse.infrastructure.persistence.trade.TradeToDbEntryMapper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class StrategyExecutionConfiguration {

    @Bean
    public StrategyExecutionFactory strategyExecutionFactory(ExecutorService marketScanningTaskExecutor,
                                                             ClockSignalDispatcher clockSignalDispatcher,
                                                             OrderExecutor orderExecutor,
                                                             TradeExecutionFactory tradeExecutionFactory,
                                                             TradeRepository tradeRepository,
                                                             StrategyExecutionEventBus eventBus) {
        return new StrategyExecutionFactory(
            marketScanningTaskExecutor, clockSignalDispatcher,
            orderExecutor, tradeExecutionFactory, tradeRepository, eventBus);
    }

    @Bean
    public ExecutorService marketScanningTaskExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean
    public ClockSignalDispatcher clockSignalDispatcher(ExecutorService clockSignalDispatchTaskExecutor) {
        return new ClockSignalDispatcher(clockSignalDispatchTaskExecutor);
    }

    @Bean
    public ExecutorService clockSignalDispatchTaskExecutor() {
        // todo: replace by appropriate ExecutorService implementation based on load analysis
        return Executors.newCachedThreadPool();
    }

    @Bean
    public OrderExecutor orderExecutor(ExchangeGateway exchangeGateway) {
        return new OrderExecutor(exchangeGateway);
    }

    @Bean
    public TradeExecutionFactory tradeExecutionFactory(ClockSignalDispatcher clockSignalDispatcher) {
        return new TradeExecutionFactory(clockSignalDispatcher);
    }

    @Bean
    public TradeRepository tradeRepository(TradeDbEntryJpaRepository dbEntryRepository) {
        var mapper = new TradeToDbEntryMapper();
        return new TradeRepositoryImpl(mapper, dbEntryRepository);
    }

    @Bean
    public StrategyExecutionEventBus eventBus() {
        return new StrategyExecutionEventBus(List.of());
    }
}
