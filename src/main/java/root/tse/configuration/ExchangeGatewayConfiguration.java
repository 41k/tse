package root.tse.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;
import root.tse.domain.strategy_execution.ExchangeGateway;
import root.tse.infrastructure.exchange_gateway.CurrencyComExchangeGateway;

@Configuration
@EnableConfigurationProperties(ExchangeGatewayConfigurationProperties.class)
public class ExchangeGatewayConfiguration {

    @Bean
    public ExchangeGateway exchangeGateway(
        ExchangeGatewayConfigurationProperties configurationProperties, RestTemplate restTemplate) {
        var retryTemplate = buildRetryTemplate(configurationProperties);
        var rateLimiter = RateLimiter.create(configurationProperties.getRateLimitPerSecond());
        var objectMapper = new ObjectMapper();
        return new CurrencyComExchangeGateway(
            configurationProperties, retryTemplate, rateLimiter, restTemplate, objectMapper);
    }

    public static RetryTemplate buildRetryTemplate(ExchangeGatewayConfigurationProperties configurationProperties) {
        var retryPolicy = new SimpleRetryPolicy(configurationProperties.getRetryAttemptsNumber());
        var backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(configurationProperties.getRetryBackoffInMilliseconds());
        var retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }
}
