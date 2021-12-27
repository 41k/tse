package root.tse

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import root.TseApp
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties
import root.tse.domain.ExchangeGateway
import root.tse.domain.clock.ClockSignalDispatcher
import root.tse.domain.clock.Interval
import root.tse.domain.order.OrderType
import root.tse.infrastructure.chain_exchange_execution.ChainExchangeTask
import root.tse.infrastructure.clock.ClockSignalPropagator
import root.tse.infrastructure.exchange_gateway.CurrencyComExchangeGateway
import root.tse.infrastructure.exchange_gateway.CurrentPriceProviderFactory
import root.tse.infrastructure.persistence.chain_exchange.ChainExchangeDbEntryJpaRepository
import root.tse.infrastructure.persistence.trade.TradeDbEntryJpaRepository
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.google.common.io.Resources.getResource
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [TseApp])
abstract class BaseFunctionalTest extends Specification {

    protected static final SERIES_LENGTH = 3
    protected static final LIMIT = SERIES_LENGTH + 1

    protected static final ORDER_EXECUTION_REQUEST_TEMPLATE = 'quantity=$AMOUNT&recvWindow=5000&side=$ORDER_TYPE&symbol=$SYMBOL&timeInForce=FOK&timestamp=$TIMESTAMP&type=MARKET&signature=$SIGNATURE'
    protected static final ORDER_EXECUTION_RESPONSE_TEMPLATE = getResource('json/order-execution-response.json').text

    @SpringBean // necessary since we should propagate clock signal manually during tests
    ClockSignalPropagator clockSignalPropagator = Mock()

    @SpringBean // necessary since we should propagate current prices manually during tests
    CurrentPriceProviderFactory currentPriceProviderFactory = Mock()

    @SpringBean // necessary since we should run chain exchange execution manually during tests
    ChainExchangeTask chainExchangeTask = Mock()

    @Autowired
    protected ClockSignalDispatcher clockSignalDispatcher

    @Autowired
    protected ExchangeGateway exchangeGateway

    @Autowired
    protected ExchangeGatewayConfigurationProperties exchangeGatewayConfigurationProperties

    @Autowired
    protected TradeDbEntryJpaRepository tradeDbEntryJpaRepository
    @Autowired
    protected ChainExchangeDbEntryJpaRepository chainExchangeDbEntryJpaRepository

    def setup() {
        tradeDbEntryJpaRepository.deleteAll()
        tradeDbEntryJpaRepository.flush()
        chainExchangeDbEntryJpaRepository.deleteAll()
        chainExchangeDbEntryJpaRepository.flush()
        removeCurrentPrices()
    }

    void mockSuccessfulSeriesRetrievalCall(String symbol, Interval interval, String responseBody) {
        stubFor(get(urlEqualTo(seriesRetrievalUrl(symbol, interval)))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(responseBody)
                .withHeader('Connection', 'close')))
    }

    void mockFailedSeriesRetrievalCall(String symbol, Interval interval) {
        stubFor(get(urlEqualTo(seriesRetrievalUrl(symbol, interval)))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader('Connection', 'close')))
    }

    String seriesRetrievalUrl(String symbol, Interval interval) {
        def intervalRepresentation = exchangeGatewayConfigurationProperties.getIntervalRepresentation(interval)
        "/klines?symbol=$symbol&interval=$intervalRepresentation&limit=$LIMIT"
    }

    void mockSuccessfulOrderExecutionCall(String requestBody, String responseBody) {
        stubFor(post(urlPathEqualTo('/order'))
            .withHeader('Content-Type', equalTo('application/x-www-form-urlencoded'))
            .withHeader('X-MBX-APIKEY', equalTo(exchangeGatewayConfigurationProperties.getApiKey()))
            .withRequestBody(equalTo(requestBody))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(responseBody)
                .withHeader('Connection', 'close')))
    }

    void mockFailedOrderExecutionCall(String requestBody) {
        stubFor(post(urlPathEqualTo('/order'))
            .withHeader('Content-Type', equalTo('application/x-www-form-urlencoded'))
            .withHeader('X-MBX-APIKEY', equalTo(exchangeGatewayConfigurationProperties.getApiKey()))
            .withRequestBody(equalTo(requestBody))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader('Connection', 'close')))
    }

    void setCurrentPrices(Map<String, Map<OrderType, Double>> currentPrices) {
        ((CurrencyComExchangeGateway) exchangeGateway).currentPrices.putAll(currentPrices)
    }

    private void removeCurrentPrices() {
        ((CurrencyComExchangeGateway) exchangeGateway).currentPrices.clear()
    }
}
