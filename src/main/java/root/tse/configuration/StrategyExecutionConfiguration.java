package root.tse.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import root.tse.domain.strategy_execution.ExchangeGateway;
import root.tse.domain.strategy_execution.MarketScanningStrategyExecutionFactory;
import root.tse.domain.strategy_execution.SimpleStrategyExecutionFactory;
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher;
import root.tse.domain.strategy_execution.clock.DefaultClockSignalDispatcher;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventSubscriber;
import root.tse.domain.strategy_execution.report.ReportBuilder;
import root.tse.domain.strategy_execution.trade.OrderExecutor;
import root.tse.domain.strategy_execution.trade.TradeExecutionFactory;
import root.tse.domain.strategy_execution.trade.TradeRepository;
import root.tse.domain.strategy_execution.trade.TradeService;
import root.tse.infrastructure.clock.ClockSignalPropagator;
import root.tse.infrastructure.persistence.trade.TradeDbEntryJpaRepository;
import root.tse.infrastructure.persistence.trade.TradeRepositoryImpl;
import root.tse.infrastructure.persistence.trade.TradeToDbEntryMapper;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class StrategyExecutionConfiguration {

    @Bean
    public SimpleStrategyExecutionFactory simpleStrategyExecutionFactory(ClockSignalDispatcher clockSignalDispatcher,
                                                                         TradeService tradeService,
                                                                         StrategyExecutionEventBus eventBus) {
        return new SimpleStrategyExecutionFactory(clockSignalDispatcher, tradeService, eventBus);
    }

    @Bean
    public MarketScanningStrategyExecutionFactory marketScanningStrategyExecutionFactory(
        ExecutorService marketScanningTaskExecutor, ClockSignalDispatcher clockSignalDispatcher,
        TradeService tradeService, TradeExecutionFactory tradeExecutionFactory, StrategyExecutionEventBus eventBus,
        Clock clock) {
        return new MarketScanningStrategyExecutionFactory(
            marketScanningTaskExecutor, clockSignalDispatcher, tradeService, tradeExecutionFactory, eventBus, clock);
    }

    @Bean
    public ExecutorService marketScanningTaskExecutor() {
        // todo: replace by appropriate ExecutorService implementation based on load analysis
        return Executors.newCachedThreadPool();
    }

    @Bean
    public ClockSignalDispatcher clockSignalDispatcher(ExecutorService clockSignalDispatchTaskExecutor) {
        return new DefaultClockSignalDispatcher(clockSignalDispatchTaskExecutor);
    }

    @Bean
    public ExecutorService clockSignalDispatchTaskExecutor() {
        // todo: replace by appropriate ExecutorService implementation based on load analysis
        return Executors.newCachedThreadPool();
    }

    @Bean
    public TradeService tradeService(OrderExecutor orderExecutor, TradeRepository tradeRepository) {
        return new TradeService(orderExecutor, tradeRepository);
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
    public StrategyExecutionEventBus eventBus(List<StrategyExecutionEventSubscriber> subscribers) {
        return new StrategyExecutionEventBus(subscribers);
    }

    @Bean
    public ClockSignalPropagator clockSignalPropagator(Clock clock, ClockSignalDispatcher clockSignalDispatcher) {
        return new ClockSignalPropagator(clock, clockSignalDispatcher);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ReportBuilder reportBuilder(TradeRepository tradeRepository) {
        return new ReportBuilder(tradeRepository);
    }
}
