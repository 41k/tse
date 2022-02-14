package root.tse.configuration.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Validated
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ConfigurationProperties(prefix = "backtest")
public class BacktestConfigurationProperties {

    private boolean enabled;
    @NotBlank
    private String dataSetName;
    @NotBlank
    private String symbol;
    @NotNull
    private Double fundsPerTrade;
    @NotNull
    private Double orderFeePercent;
}
