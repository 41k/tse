package root.tse.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import root.tse.domain.IdGenerator;
import root.tse.domain.clock.SequentialClockSignalDispatcher;
import root.tse.domain.backtest.BacktestService;
import root.tse.domain.backtest.DataSetService;
import root.tse.domain.strategy_execution.SimpleStrategyExecutionFactory;
import root.tse.domain.event.EventBus;
import root.tse.domain.strategy_execution.report.ReportBuilder;
import root.tse.domain.strategy_execution.trade.TradeService;
import root.tse.infrastructure.persistence.data_set.BarRowMapper;
import root.tse.infrastructure.persistence.data_set.DataSetServiceImpl;

@Configuration
public class BacktestConfiguration {

    @Bean
    public BacktestService backtestService(
        IdGenerator idGenerator,
        TradeService tradeService,
        EventBus eventBus,
        ReportBuilder reportBuilder
    ) {
        var clockSignalDispatcher = new SequentialClockSignalDispatcher();
        var strategyExecutionFactory = new SimpleStrategyExecutionFactory(idGenerator, clockSignalDispatcher, tradeService, eventBus);
        return new BacktestService(strategyExecutionFactory, clockSignalDispatcher, reportBuilder);
    }

    @Bean
    public DataSetService dataSetService(JdbcTemplate jdbcTemplate) {
        return new DataSetServiceImpl(jdbcTemplate, new BarRowMapper());
    }
}
