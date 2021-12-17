package root.tse.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import root.tse.configuration.properties.TelegramConfigurationProperties;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventSubscriber;
import root.tse.infrastructure.telegram.TelegramApiClient;
import root.tse.infrastructure.telegram.TelegramEventPublisher;

@Configuration
@EnableConfigurationProperties(TelegramConfigurationProperties.class)
@ConditionalOnProperty(value = "telegram.enabled", havingValue = "true")
public class TelegramConfiguration {

    @Bean
    public StrategyExecutionEventSubscriber telegramEventPublisher(TelegramApiClient telegramApiClient) {
        return new TelegramEventPublisher(telegramApiClient);
    }

    @Bean
    public TelegramApiClient telegramApiClient(TelegramConfigurationProperties properties, RestTemplate restTemplate) {
        return new TelegramApiClient(properties, restTemplate);
    }
}
