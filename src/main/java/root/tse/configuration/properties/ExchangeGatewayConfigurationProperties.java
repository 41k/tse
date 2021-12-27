package root.tse.configuration.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import root.tse.application.model.ChainExchangeExecutionSettings;
import root.tse.domain.clock.Interval;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Data
@Validated
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ConfigurationProperties(prefix = "exchange-gateway")
public class ExchangeGatewayConfigurationProperties {

    @NotBlank
    private String apiKey;
    @NotBlank
    private String secretKey;
    @NotBlank
    private String seriesUri;
    @NotBlank
    private String orderUri;
    @NotBlank
    private String wssConnectionUri;
    @NotBlank
    private String currentPriceWssEndpoint;
    @NotNull
    private Double orderFeePercent;
    @NotEmpty
    private Map<Interval, String> intervalToRepresentationMap;
    @NotBlank
    private String assetCodeDelimiter;
    @Min(5)
    private int numberOfAmountSelectionSteps;
    @NotEmpty
    private Map<String, SymbolSettings> symbolSettings;
    @NotEmpty
    private Map<Integer, List<String>> assetChains;

    @Min(1)
    private int rateLimitPerSecond;
    @Min(1)
    private int retryAttemptsNumber;
    @Min(1)
    private int retryBackoffInMilliseconds;

    public String getIntervalRepresentation(Interval interval) {
        return Optional.ofNullable(intervalToRepresentationMap.get(interval)).orElseThrow(() ->
            new NoSuchElementException(format(">>> no representation configured for %s interval.", interval.name())));
    }

    public Collection<String> getSymbolNames() {
        return symbolSettings.values().stream().map(SymbolSettings::getName).collect(Collectors.toList());
    }

    public Map<String, Integer> getSymbolToPrecisionMap() {
        return symbolSettings.values().stream()
            .collect(Collectors.toMap(SymbolSettings::getName, SymbolSettings::getPrecision));
    }

    public ChainExchangeExecutionSettings getChainExchangeExecutionSettings() {
        return ChainExchangeExecutionSettings.builder()
            .assetChains(assetChains)
            .assetCodeDelimiter(assetCodeDelimiter)
            .symbolToPrecisionMap(getSymbolToPrecisionMap())
            .orderFeePercent(orderFeePercent)
            .nAmountSelectionSteps(numberOfAmountSelectionSteps)
            .build();
    }

    @Data
    @Valid
    public static class SymbolSettings {
        @NotBlank
        private String name;
        @Min(1)
        private Integer precision;
    }
}
