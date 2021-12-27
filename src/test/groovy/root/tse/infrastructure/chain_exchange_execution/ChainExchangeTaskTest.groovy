package root.tse.infrastructure.chain_exchange_execution

import root.tse.domain.chain_exchange_execution.ChainExchangeExecution
import spock.lang.Specification

import static root.tse.util.TestUtils.CHAIN_EXCHANGE_EXECUTION_CONTEXT

class ChainExchangeTaskTest extends Specification {

    private chainExchangeExecution1 = Mock(ChainExchangeExecution)
    private chainExchangeExecution2 = Mock(ChainExchangeExecution)
    private chainExchangeExecutionStore = [
        1 : chainExchangeExecution1,
        2 : chainExchangeExecution2
    ]

    private chainExchangeTask = new ChainExchangeTask(chainExchangeExecutionStore)

    def 'should run all active chain exchange executions'() {
        when:
        chainExchangeTask.run()

        then:
        1 * chainExchangeExecution1.run()
        1 * chainExchangeExecution2.run()
        0 * _
    }

    def 'should swallow exceptions thrown during any run of chain exchange execution'() {
        when:
        chainExchangeTask.run()

        then:
        1 * chainExchangeExecution1.run() >> { throw new RuntimeException('chain exchange execution run error') }
        1 * chainExchangeExecution1.getContext() >> CHAIN_EXCHANGE_EXECUTION_CONTEXT
        1 * chainExchangeExecution2.run()
        0 * _

        and:
        noExceptionThrown()
    }
}
