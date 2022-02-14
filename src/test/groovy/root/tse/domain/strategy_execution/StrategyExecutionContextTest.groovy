package root.tse.domain.strategy_execution

import root.tse.domain.clock.Interval
import spock.lang.Specification

class StrategyExecutionContextTest extends Specification {

    def 'should init marketScanningInterval = ONE_DAY by default'() {
        expect:
        StrategyExecutionContext.builder().build().marketScanningInterval == Interval.ONE_DAY
    }

    def 'should init allowedNumberOfSimultaneouslyOpenedTrades = 1 by default'() {
        expect:
        StrategyExecutionContext.builder().build().allowedNumberOfSimultaneouslyOpenedTrades == 1
    }
}
