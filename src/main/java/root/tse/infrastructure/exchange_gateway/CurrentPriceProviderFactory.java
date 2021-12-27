package root.tse.infrastructure.exchange_gateway;

import lombok.RequiredArgsConstructor;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

@RequiredArgsConstructor
public class CurrentPriceProviderFactory {

    private final URI wssConnectionUri;
    private final String currentPriceWssEndpoint;
    private final Collection<String> symbols;

    public CurrentPriceProviderFactory(ExchangeGatewayConfigurationProperties properties) throws URISyntaxException {
        this.wssConnectionUri = new URI(properties.getWssConnectionUri());
        this.currentPriceWssEndpoint = properties.getCurrentPriceWssEndpoint();
        this.symbols = properties.getSymbolNames();
    }

    public CurrentPriceProvider create(CurrencyComExchangeGateway exchangeGateway) {
        var currentPriceProvider = new CurrentPriceProvider(wssConnectionUri, currentPriceWssEndpoint, symbols, exchangeGateway);
        currentPriceProvider.connect();
        return currentPriceProvider;
    }
}
