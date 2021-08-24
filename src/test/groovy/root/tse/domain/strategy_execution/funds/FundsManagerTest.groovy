package root.tse.domain.strategy_execution.funds

import spock.lang.Specification

class FundsManagerTest extends Specification {

    private static final AMOUNT = 1.5d
    private static final PRICE = 3015.97d

    private funds = Mock(Funds)
    private fundsRepository = Mock(FundsRepository)
    private fundsManager = new FundsManager(fundsRepository)

    def 'should decrease funds and provide trade amount'() {
        when:
        def acquiredTradeAmount = fundsManager.acquireFundsAndProvideTradeAmount(PRICE)

        then:
        1 * fundsRepository.get() >> funds
        1 * funds.areNotEnough() >> false
        1 * funds.decrease()
        1 * fundsRepository.save(funds)
        1 * funds.calculateTradeAmount(PRICE) >> AMOUNT
        0 * _

        and:
        acquiredTradeAmount == AMOUNT
    }

    def 'should not decrease funds and should not provide trade amount if funds are not enough'() {
        when:
        def acquiredTradeAmount = fundsManager.acquireFundsAndProvideTradeAmount(PRICE)

        then:
        1 * fundsRepository.get() >> funds
        1 * funds.areNotEnough() >> true
        0 * _

        and:
        acquiredTradeAmount == null
    }

    def 'should return funds'() {
        when:
        fundsManager.returnFunds()

        then:
        1 * fundsRepository.get() >> funds
        1 * funds.increase()
        1 * fundsRepository.save(funds)
        0 * _
    }
}
