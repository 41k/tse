package root.tse.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import root.tse.application.strategy_execution.StrategyExecutionService;
import root.tse.application.strategy_execution.rule.EntryRuleBuilder;
import root.tse.application.strategy_execution.rule.ExitRuleBuilder;
import root.tse.application.strategy_execution.rule.RuleService;
import root.tse.application.strategy_execution.rule.entry.ImmediatelySatisfiedOnlyOnce_EntryRuleBuilder;
import root.tse.application.strategy_execution.rule.entry.PriceIs_EntryRuleBuilder;
import root.tse.application.strategy_execution.rule.exit.PriceIs_ExitRuleBuilder;
import root.tse.application.strategy_execution.rule.exit.ProfitOrLossValueIs_ExitRuleBuilder;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.IdGenerator;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.event.EventBus;
import root.tse.domain.strategy_execution.MarketScanningStrategyExecutionFactory;
import root.tse.domain.strategy_execution.SimpleStrategyExecutionFactory;
import root.tse.domain.strategy_execution.StrategyExecution;
import root.tse.domain.strategy_execution.report.ReportBuilder;
import root.tse.domain.strategy_execution.trade.TradeExecutionFactory;
import root.tse.domain.strategy_execution.trade.TradeRepository;
import root.tse.domain.strategy_execution.trade.TradeService;
import root.tse.infrastructure.persistence.trade.TradeDbEntryJpaRepository;
import root.tse.infrastructure.persistence.trade.TradeRepositoryImpl;
import root.tse.infrastructure.persistence.trade.TradeToDbEntryMapper;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ExchangeGatewayConfigurationProperties.class)
public class StrategyExecutionConfiguration {

    @Bean
    public StrategyExecutionService strategyExecutionService(
        ExchangeGatewayConfigurationProperties exchangeGatewayProperties,
        RuleService ruleService,
        SimpleStrategyExecutionFactory simpleStrategyExecutionFactory,
        Map<String, StrategyExecution> strategyExecutionsStore,
        ReportBuilder reportBuilder
    ) {
        return new StrategyExecutionService(
            ruleService, simpleStrategyExecutionFactory, strategyExecutionsStore, reportBuilder);
    }

    @Bean
    public RuleService ruleService(
        @Qualifier("entryRuleBuilders") Map<String, EntryRuleBuilder> entryRuleBuilders,
        @Qualifier("exitRuleBuilders") Map<String, ExitRuleBuilder> exitRuleBuilders
    ) {
        return new RuleService(entryRuleBuilders, exitRuleBuilders);
    }

    @Bean
    public Map<String, StrategyExecution> strategyExecutionsStore() {
        return new HashMap<>();
    }

    @Bean
    public SimpleStrategyExecutionFactory simpleStrategyExecutionFactory(
        IdGenerator idGenerator,
        ClockSignalDispatcher clockSignalDispatcher,
        TradeService tradeService,
        EventBus eventBus
    ) {
        return new SimpleStrategyExecutionFactory(idGenerator, clockSignalDispatcher, tradeService, eventBus);
    }

    @Bean
    public MarketScanningStrategyExecutionFactory marketScanningStrategyExecutionFactory(
        IdGenerator idGenerator,
        ExecutorService marketScanningTaskExecutor,
        ClockSignalDispatcher clockSignalDispatcher,
        TradeService tradeService,
        TradeExecutionFactory tradeExecutionFactory,
        EventBus eventBus,
        Clock clock
    ) {
        return new MarketScanningStrategyExecutionFactory(
            idGenerator, marketScanningTaskExecutor, clockSignalDispatcher,
            tradeService, tradeExecutionFactory, eventBus, clock);
    }

    @Bean
    public ExecutorService marketScanningTaskExecutor() {
        // todo: replace by appropriate ExecutorService implementation based on load analysis
        return Executors.newCachedThreadPool();
    }

    @Bean
    public TradeService tradeService(IdGenerator idGenerator,
                                     ExchangeGateway exchangeGateway,
                                     TradeRepository tradeRepository) {
        return new TradeService(idGenerator, exchangeGateway, tradeRepository);
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
    public ReportBuilder reportBuilder(TradeRepository tradeRepository) {
        return new ReportBuilder(tradeRepository);
    }


    // Rule builders

    @Bean
    public Map<String, EntryRuleBuilder> entryRuleBuilders(List<EntryRuleBuilder> ruleBuilders) {
        return ruleBuilders.stream().collect(Collectors.toMap(
            ruleBuilder -> ruleBuilder.getRuleDescription().getId(),
            Function.identity()));
    }
    @Bean
    public Map<String, ExitRuleBuilder> exitRuleBuilders(List<ExitRuleBuilder> ruleBuilders) {
        return ruleBuilders.stream().collect(Collectors.toMap(
            ruleBuilder -> ruleBuilder.getRuleDescription().getId(),
            Function.identity()));
    }

    // Entry rule builders

    @Bean
    public EntryRuleBuilder priceIs_EntryRuleBuilder(ExchangeGateway exchangeGateway) {
        return new PriceIs_EntryRuleBuilder(exchangeGateway);
    }
    @Bean
    public EntryRuleBuilder immediatelySatisfiedOnlyOnce_EntryRuleBuilder() {
        return new ImmediatelySatisfiedOnlyOnce_EntryRuleBuilder();
    }

    // Exit rule builders

    @Bean
    public ExitRuleBuilder priceIs_ExitRuleBuilder(ExchangeGateway exchangeGateway) {
        return new PriceIs_ExitRuleBuilder(exchangeGateway);
    }
    @Bean
    public ExitRuleBuilder profitOrLossValueIs_ExitRuleBuilder(ExchangeGateway exchangeGateway) {
        return new ProfitOrLossValueIs_ExitRuleBuilder(exchangeGateway);
    }
}
