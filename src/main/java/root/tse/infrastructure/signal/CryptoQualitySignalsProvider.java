package root.tse.infrastructure.signal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import root.tse.infrastructure.persistence.signal.SignalRepository;

import java.util.Optional;

// API docs: https://cryptoqualitysignals.com/index.php/api/
@Component
@Slf4j
@RequiredArgsConstructor
public class CryptoQualitySignalsProvider {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SignalRepository signalRepository;

    @Scheduled(fixedDelay = 310000)
    public void tryToGetSignal() {
        var uri = UriComponentsBuilder.fromHttpUrl("https://api.cryptoqualitysignals.com/v1/getSignal")
            .queryParam("api_key", "FREE")
            .queryParam("interval", 20)
            .toUriString();
        var response = restTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY, String.class);
        Optional.ofNullable(response.getBody()).ifPresent(responseBody -> {
            signalRepository.save("crypto-quality-signals", responseBody);
            log.info(">>> {}", responseBody);
        });
    }
}
