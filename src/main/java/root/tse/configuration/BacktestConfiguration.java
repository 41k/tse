package root.tse.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import root.tse.domain.backtest.BacktestClockSignalDispatcher;
import root.tse.domain.backtest.BacktestService;
import root.tse.domain.backtest.DataSetService;
import root.tse.domain.strategy_execution.SimpleStrategyExecutionFactory;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus;
import root.tse.domain.strategy_execution.report.ReportBuilder;
import root.tse.domain.strategy_execution.trade.TradeService;
import root.tse.infrastructure.persistence.data_set.BarRowMapper;
import root.tse.infrastructure.persistence.data_set.DataSetServiceImpl;

@Configuration
public class BacktestConfiguration {

    @Bean
    public BacktestService backtestService(TradeService tradeService,
                                           StrategyExecutionEventBus eventBus,
                                           ReportBuilder reportBuilder) {
        var clockSignalDispatcher = new BacktestClockSignalDispatcher();
        var strategyExecutionFactory = new SimpleStrategyExecutionFactory(clockSignalDispatcher, tradeService, eventBus);
        return new BacktestService(strategyExecutionFactory, clockSignalDispatcher, reportBuilder);
    }

    @Bean
    public DataSetService dataSetService(JdbcTemplate jdbcTemplate) {
        return new DataSetServiceImpl(jdbcTemplate, new BarRowMapper());
    }
}
