package root.tse.domain.backtest

import root.tse.domain.strategy_execution.SimpleStrategyExecution
import root.tse.domain.strategy_execution.SimpleStrategyExecutionFactory
import root.tse.domain.strategy_execution.StrategyExecutionContext
import root.tse.domain.strategy_execution.clock.ClockSignal
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.report.Report
import root.tse.domain.strategy_execution.report.ReportBuilder
import spock.lang.Specification

import static root.tse.domain.strategy_execution.Interval.*

class BacktestServiceTest extends Specification {

    private static final START_TIMESTAMP = 1637366100000L // 2021-11-19 23:55:00
    private static final END_TIMESTAMP = 1637366400000L // 2021-11-20 00:00:00

    private backtestExchangeGateway = Mock(BacktestExchangeGateway)
    private backtestContext = Mock(BacktestContext)
    private strategyExecutionContext = StrategyExecutionContext.builder().build()
    private simpleStrategyExecution = Mock(SimpleStrategyExecution)
    private strategyExecutionFactory = Mock(SimpleStrategyExecutionFactory)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private reportBuilder = Mock(ReportBuilder)

    private backtestService = new BacktestService(strategyExecutionFactory, clockSignalDispatcher, reportBuilder)

    def 'should run backtest correctly'() {
        given:
        def expectedBacktestReport = Report.builder().nTrades(10).build()

        when:
        def backtestReport = backtestService.runBacktest(backtestContext)

        then: 'create strategy execution'
        1 * backtestContext.getStrategyExecutionContext() >> strategyExecutionContext
        1 * strategyExecutionFactory.create(strategyExecutionContext) >> simpleStrategyExecution

        and: 'run strategy execution'
        1 * simpleStrategyExecution.start()
        1 * backtestContext.getBacktestExchangeGateway() >> backtestExchangeGateway
        1 * backtestContext.getStartTimestamp() >> START_TIMESTAMP
        1 * backtestContext.getEndTimestamp() >> END_TIMESTAMP
        // 23:55:00 2021-11-19
        1 * backtestExchangeGateway.setCurrentTimestamp(1637366100000L)
        1 * clockSignalDispatcher.dispatch(new ClockSignal(ONE_MINUTE, 1637366100000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(FIVE_MINUTES, 1637366100000L))
        // 23:56:00 2021-11-19
        1 * backtestExchangeGateway.setCurrentTimestamp(1637366160000L)
        1 * clockSignalDispatcher.dispatch(new ClockSignal(ONE_MINUTE, 1637366160000L))
        // 23:57:00 2021-11-19
        1 * backtestExchangeGateway.setCurrentTimestamp(1637366220000L)
        1 * clockSignalDispatcher.dispatch(new ClockSignal(ONE_MINUTE, 1637366220000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(THREE_MINUTES, 1637366220000L))
        // 23:58:00 2021-11-19
        1 * backtestExchangeGateway.setCurrentTimestamp(1637366280000L)
        1 * clockSignalDispatcher.dispatch(new ClockSignal(ONE_MINUTE, 1637366280000L))
        // 23:59:00 2021-11-19
        1 * backtestExchangeGateway.setCurrentTimestamp(1637366340000L)
        1 * clockSignalDispatcher.dispatch(new ClockSignal(ONE_MINUTE, 1637366340000L))
        // 00:00:00 2021-11-20
        1 * backtestExchangeGateway.setCurrentTimestamp(1637366400000L)
        1 * clockSignalDispatcher.dispatch(new ClockSignal(ONE_MINUTE, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(THREE_MINUTES, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(FIVE_MINUTES, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(FIFTEEN_MINUTES, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(THIRTY_MINUTES, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(ONE_HOUR, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(TWO_HOURS, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(FOUR_HOURS, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(SIX_HOURS, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(EIGHT_HOURS, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(TWELVE_HOURS, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(ONE_DAY, 1637366400000L))
        1 * clockSignalDispatcher.dispatch(new ClockSignal(THREE_DAYS, 1637366400000L))
        1 * simpleStrategyExecution.stop()

        and: 'build backtest report'
        1 * reportBuilder.build(simpleStrategyExecution) >> expectedBacktestReport
        0 * _

        and:
        backtestReport == expectedBacktestReport

        when:
        backtestReport = backtestService.getReport()

        then:
        0 * _

        and:
        backtestReport == expectedBacktestReport
    }
}
