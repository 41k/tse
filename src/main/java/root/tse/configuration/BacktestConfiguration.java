package root.tse.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import root.tse.configuration.properties.BacktestConfigurationProperties;
import root.tse.domain.IdGenerator;
import root.tse.domain.backtest.BacktestExchangeGateway;
import root.tse.domain.backtest.BacktestService;
import root.tse.domain.backtest.DataSetService;
import root.tse.domain.clock.SequentialClockSignalDispatcher;
import root.tse.domain.event.EventBus;
import root.tse.domain.strategy_execution.SimpleStrategyExecutionFactory;
import root.tse.domain.strategy_execution.report.ReportBuilder;
import root.tse.domain.strategy_execution.trade.TradeService;
import root.tse.infrastructure.persistence.data_set.BarRowMapper;
import root.tse.infrastructure.persistence.data_set.DataSetServiceImpl;

@Configuration
@EnableConfigurationProperties(BacktestConfigurationProperties.class)
public class BacktestConfiguration {

    @Bean
    public BacktestService backtestService(
        BacktestExchangeGateway backtestExchangeGateway,
        IdGenerator idGenerator,
        TradeService tradeService,
        EventBus eventBus,
        ReportBuilder reportBuilder
    ) {
        var clockSignalDispatcher = new SequentialClockSignalDispatcher();
        var strategyExecutionFactory = new SimpleStrategyExecutionFactory(idGenerator, clockSignalDispatcher, tradeService, eventBus);
        return new BacktestService(backtestExchangeGateway, strategyExecutionFactory, clockSignalDispatcher, reportBuilder);
    }

    @Bean
    public BacktestExchangeGateway backtestExchangeGateway(
        BacktestConfigurationProperties backtestProperties,
        DataSetService dataSetService
    ) {
        return new BacktestExchangeGateway(backtestProperties, dataSetService);
    }

    @Bean
    public DataSetService dataSetService(JdbcTemplate jdbcTemplate) {
        return new DataSetServiceImpl(jdbcTemplate, new BarRowMapper());
    }
}
