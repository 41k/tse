package root.tse.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import root.tse.configuration.properties.BacktestConfigurationProperties;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.backtest.BacktestExchangeGateway;
import root.tse.infrastructure.exchange_gateway.CurrencyComExchangeGateway;
import root.tse.infrastructure.exchange_gateway.CurrentPriceProviderFactory;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties({
    BacktestConfigurationProperties.class,
    ExchangeGatewayConfigurationProperties.class
})
public class ExchangeGatewayConfiguration {

    @Bean
    public ExchangeGateway exchangeGateway(
        BacktestConfigurationProperties backtest,
        BacktestExchangeGateway backtestExchangeGateway,
        ExchangeGatewayConfigurationProperties exchangeGatewayProperties,
        CurrentPriceProviderFactory currentPriceProviderFactory,
        RestTemplate restTemplate,
        Clock clock
    ) {
        if (backtest.isEnabled()) {
            return backtestExchangeGateway;
        }
        var retryTemplate = buildRetryTemplate(exchangeGatewayProperties);
        var rateLimiter = RateLimiter.create(exchangeGatewayProperties.getRateLimitPerSecond());
        var objectMapper = new ObjectMapper();
        return new CurrencyComExchangeGateway(
            exchangeGatewayProperties, currentPriceProviderFactory,
            retryTemplate, rateLimiter, restTemplate, objectMapper, clock);
    }

    @Bean
    @SneakyThrows
    public CurrentPriceProviderFactory currentPriceProviderFactory(ExchangeGatewayConfigurationProperties properties) {
        return new CurrentPriceProviderFactory(properties);
    }

    private static RetryTemplate buildRetryTemplate(ExchangeGatewayConfigurationProperties properties) {
        var retryPolicy = new SimpleRetryPolicy(properties.getRetryAttemptsNumber());
        var backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(properties.getRetryBackoffInMilliseconds());
        var retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }
}
