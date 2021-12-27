package root.tse.domain.backtest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.clock.Interval;
import root.tse.domain.strategy_execution.SimpleStrategyExecutionFactory;
import root.tse.domain.strategy_execution.StrategyExecution;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.strategy_execution.report.Report;
import root.tse.domain.strategy_execution.report.ReportBuilder;

import static root.tse.domain.clock.Interval.ONE_MINUTE;

@Slf4j
@RequiredArgsConstructor
public class BacktestService {

    private final SimpleStrategyExecutionFactory strategyExecutionFactory;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final ReportBuilder reportBuilder;

    @Getter
    private Report report;

    public Report runBacktest(BacktestContext context) {
        var strategyExecution = strategyExecutionFactory.create(context.getStrategyExecutionContext());
        runStrategyExecution(strategyExecution, context);
        report = reportBuilder.build(strategyExecution);
        return report;
    }

    private void runStrategyExecution(StrategyExecution strategyExecution, BacktestContext context) {
        strategyExecution.start();
        var exchangeGateway = context.getBacktestExchangeGateway();
        var startTimestamp = context.getStartTimestamp();
        var endTimestamp = context.getEndTimestamp();
        for (var currentTimestamp = startTimestamp; currentTimestamp <= endTimestamp; currentTimestamp = currentTimestamp + ONE_MINUTE.inMillis()) {
            exchangeGateway.setCurrentTimestamp(currentTimestamp);
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
