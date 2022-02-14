package root.tse.domain.backtest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.clock.Interval;
import root.tse.domain.strategy_execution.SimpleStrategyExecutionFactory;
import root.tse.domain.strategy_execution.StrategyExecution;
import root.tse.domain.strategy_execution.StrategyExecutionContext;
import root.tse.domain.strategy_execution.report.Report;
import root.tse.domain.strategy_execution.report.ReportBuilder;

import static root.tse.domain.clock.Interval.ONE_MINUTE;

@Slf4j
@RequiredArgsConstructor
public class BacktestService {

    private final BacktestExchangeGateway backtestExchangeGateway;
    private final SimpleStrategyExecutionFactory strategyExecutionFactory;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final ReportBuilder reportBuilder;

    @Getter
    private Report report;

    public Report runBacktest(StrategyExecutionContext strategyExecutionContext) {
        var strategyExecution = strategyExecutionFactory.create(strategyExecutionContext);
        runStrategyExecution(strategyExecution);
        report = reportBuilder.build(strategyExecution);
        return report;
    }

    private void runStrategyExecution(StrategyExecution strategyExecution) {
        strategyExecution.start();
        var startTimestamp = backtestExchangeGateway.getStartTimestamp();
        var endTimestamp = backtestExchangeGateway.getEndTimestamp();
        for (var currentTimestamp = startTimestamp; currentTimestamp <= endTimestamp; currentTimestamp = currentTimestamp + ONE_MINUTE.inMillis()) {
            backtestExchangeGateway.setCurrentTimestamp(currentTimestamp);
            for (Interval interval : Interval.values()) {
                if (currentTimestamp % interval.inMillis() == 0) {
                    var clockSignal = new ClockSignal(interval, currentTimestamp);
                    clockSignalDispatcher.dispatch(clockSignal);
                }
            }
        }
        strategyExecution.stop();
    }
}
