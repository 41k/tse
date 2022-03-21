package root.tse

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.ta4j.core.BarSeries
import root.TseApp
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties
import root.tse.domain.ExchangeGateway
import root.tse.domain.clock.ClockSignalDispatcher
import root.tse.domain.clock.Interval
import root.tse.domain.order.Order
import root.tse.domain.order.OrderType
import root.tse.infrastructure.chain_exchange_execution.ChainExchangeTask
import root.tse.infrastructure.clock.ClockSignalPropagator
import root.tse.infrastructure.exchange_gateway.CurrentPriceProviderFactory
import root.tse.infrastructure.persistence.chain_exchange.ChainExchangeDbEntryJpaRepository
import root.tse.infrastructure.persistence.trade.TradeDbEntryJpaRepository
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [TseApp, BaseTestContextConfiguration])
abstract class BaseFunctionalTest extends Specification {

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
    }

    ExchangeGatewayMock exchangeGatewayMock() {
        (ExchangeGatewayMock) exchangeGateway
    }

    static class ExchangeGatewayMock implements ExchangeGateway {
        Double orderFeePercent
        Map<String, Map<OrderType, Double>> currentPrices
        boolean orderExecutionSuccess
        @Override
        Double getOrderFeePercent() { orderFeePercent }
        @Override
        Optional<BarSeries> getSeries(String symbol, Interval interval, Integer seriesLength) { Optional.empty() }
        @Override
        Optional<Map<String, Map<OrderType, Double>>> getCurrentPrices(List<String> symbols) { Optional.ofNullable(currentPrices) }
        @Override
        Optional<Order> tryToExecute(Order order) {
            orderExecutionSuccess ?
                Optional.of(order.toBuilder().price(currentPrices.get(order.symbol).get(order.type)).build()) :
                Optional.<Order>empty()
        }
        void reset() {
            orderFeePercent = null
            currentPrices = null
            orderExecutionSuccess = false
        }
    }

    @TestConfiguration
    static class BaseTestContextConfiguration {
        @Bean
        ExchangeGateway exchangeGateway() { new ExchangeGatewayMock() }
    }
}
