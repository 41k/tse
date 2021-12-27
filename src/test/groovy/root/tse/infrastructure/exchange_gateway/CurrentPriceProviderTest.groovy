package root.tse.infrastructure.exchange_gateway

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.util.TestUtils.*

class CurrentPriceProviderTest extends Specification {

    private exchangeGateway = Mock(CurrencyComExchangeGateway)

    private CurrentPriceProvider currentPriceProvider

    def setup() {
        currentPriceProvider = new CurrentPriceProvider(new URI('wss://uri'),'wss-endpoint', ['symbols'], exchangeGateway)
    }

    def 'should process wss message'() {
        given:
        def message = new ObjectMapper().writeValueAsString([
            'payload' : [
                'symbolName' : SYMBOL_1,
                'ofr' : PRICE_1,
                'bid' : PRICE_2
            ]
        ])

        when:
        currentPriceProvider.onMessage(message)

        then:
        1 * exchangeGateway.acceptCurrentPrices([
            (SYMBOL_1) : [(BUY) : PRICE_1, (SELL) : PRICE_2]
        ])
        0 * _
    }

    def 'should swallow exceptions thrown during message processing'() {
        when:
        currentPriceProvider.onMessage('invalid-message')

        then:
        0 * _

        and:
        noExceptionThrown()
    }

    def 'should be restarted if wss connection was closed'() {
        given:
        assert !currentPriceProvider.stopped.get()

        when:
        currentPriceProvider.onClose(1, 'server-error', true)

        then:
        1 * exchangeGateway.startNewCurrentPriceProvider()
        0 * _

        and:
        currentPriceProvider.stopped.get()
    }

    def 'should be restarted in case of error'() {
        given:
        assert !currentPriceProvider.stopped.get()

        when:
        currentPriceProvider.onError(new RuntimeException('server-error'))

        then:
        1 * exchangeGateway.startNewCurrentPriceProvider()
        0 * _

        and:
        currentPriceProvider.stopped.get()
    }
}
