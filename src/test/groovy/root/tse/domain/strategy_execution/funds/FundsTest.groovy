package root.tse.domain.strategy_execution.funds

import spock.lang.Specification
import spock.lang.Unroll

class FundsTest extends Specification {

    @Unroll
    def 'should return right funds availability status'() {
        given:
        def funds = Funds.builder()
            .availableFunds(availableFunds)
            .fundsPerTrade(fundsPerTrade)
            .build()

        expect:
        funds.areNotEnough() == result

        where:
        availableFunds | fundsPerTrade || result
        12.43d         | 20d           || true
        100.34d        | 50d           || false
    }

    def 'should decrease funds correctly'() {
        given:
        def funds = Funds.builder()
            .availableFunds(23d)
            .fundsPerTrade(15d)
            .build()

        when:
        funds.decrease()

        then:
        funds.getAvailableFunds() == 8d
    }

    def 'should increase funds correctly'() {
        given:
        def funds = Funds.builder()
            .availableFunds(19d)
            .fundsPerTrade(10d)
            .build()

        when:
        funds.increase()

        then:
        funds.getAvailableFunds() == 29d
    }

    @Unroll
    def 'should calculate trade amount'() {
        given:
        def funds = Funds.builder()
            .fundsPerTrade(fundsPerTrade)
            .build()

        expect:
        funds.calculateTradeAmount(price) == amount

        where:
        fundsPerTrade | price || amount
        100d          | 25d   || 4d
        3d            | 100d  || 0.03d
    }
}
