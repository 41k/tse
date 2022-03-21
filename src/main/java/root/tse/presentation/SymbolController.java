package root.tse.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties.SymbolSettings;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/symbols")
@RequiredArgsConstructor
public class SymbolController {

    private final ExchangeGatewayConfigurationProperties exchangeGatewayConfigurationProperties;

    @GetMapping
    public Collection<String> getSymbols() {
        return exchangeGatewayConfigurationProperties.getSymbolSettings()
            .values().stream().map(SymbolSettings::getName).collect(Collectors.toList());
    }
}
