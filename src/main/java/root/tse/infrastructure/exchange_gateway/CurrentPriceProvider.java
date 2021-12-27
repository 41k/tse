package root.tse.infrastructure.exchange_gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static root.tse.domain.order.OrderType.BUY;
import static root.tse.domain.order.OrderType.SELL;

@Slf4j
public class CurrentPriceProvider extends WebSocketClient {

    private static final String SYMBOL = "symbolName";
    private static final String BUY_PRICE = "ofr";
    private static final String SELL_PRICE = "bid";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String destination;
    private final Collection<String> symbols;
    private final CurrencyComExchangeGateway exchangeGateway;
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public CurrentPriceProvider(URI wssConnectionUri,
                                String destination,
                                Collection<String> symbols,
                                CurrencyComExchangeGateway exchangeGateway) {
        super(wssConnectionUri);
        this.destination = destination;
        this.symbols = symbols;
        this.exchangeGateway = exchangeGateway;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        log.info(">>> wss connection for current price retrieval has been opened");
        send(subscriptionRequest(symbols));
    }

    @Override
    public void onMessage(String message) {
        log.trace(">>> wss message received: {}", message);
        try {
            var payload = objectMapper.readValue(message, WssIncomingMessage.class).getPayload();
            var symbol = Optional.ofNullable(payload.get(SYMBOL)).map(Objects::toString).orElseThrow();
            var buyPrice = Optional.ofNullable(payload.get(BUY_PRICE)).map(this::asDouble).orElseThrow();
            var sellPrice = Optional.ofNullable(payload.get(SELL_PRICE)).map(this::asDouble).orElseThrow();
            var currentPrices = Map.of(symbol, Map.of(
                BUY, buyPrice,
                SELL, sellPrice
            ));
            exchangeGateway.acceptCurrentPrices(currentPrices);
        } catch (Exception skippedException) {}
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.warn(">>> wss connection has been closed [code: {}, reason: {}, by remote: {}], provider will be restarted", code, reason, remote);
        restart();
    }

    @Override
    public void onError(Exception e) {
        log.error(">>> current price retrieval error [{}: {}], provider will be restarted", e.getClass().getSimpleName(), e.getMessage());
        restart();
    }

    @SneakyThrows
    private String subscriptionRequest(Collection<String> symbols) {
        return objectMapper.writeValueAsString(new SubscriptionRequest(destination, symbols));
    }

    private Double asDouble(Object value) {
        return Double.valueOf(value.toString());
    }

    private void restart() {
        if (stopped.compareAndSet(false, true)) {
            close();
            exchangeGateway.startNewCurrentPriceProvider();
        }
    }

    @Getter
    public static class SubscriptionRequest {

        private final String destination;
        private final Payload payload;

        public SubscriptionRequest(String destination, Collection<String> symbols) {
            this.destination = destination;
            this.payload = new Payload(symbols);
        }

        @Value
        public static class Payload {
            Collection<String> symbols;
        }
    }
}
