package root.tse.configuration.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import root.tse.domain.strategy_execution.Interval;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.lang.String.format;

@Data
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
    @NotNull
    private Double transactionFeePercent;
    @NotEmpty
    private Map<Interval, String> intervalToRepresentationMap;

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
}
