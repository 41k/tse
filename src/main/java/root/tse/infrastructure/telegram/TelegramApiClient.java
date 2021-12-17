package root.tse.infrastructure.telegram;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.client.RestTemplate;
import root.tse.configuration.properties.TelegramConfigurationProperties;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class TelegramApiClient {

    public static final String LINE_BREAK = "-----";
    private static final String LINE_BREAK_URL_ENCODED = "%0A";

    private final TelegramConfigurationProperties properties;
    private final RestTemplate restTemplate;

    public void sendMessage(String message) {
        var urlEncodedMessage = encodeMessage(message);
        var uri = properties.buildUri(urlEncodedMessage);
        restTemplate.getForObject(uri, String.class);
    }

    @SneakyThrows
    private String encodeMessage(String message) {
        return URLEncoder.encode(message, StandardCharsets.UTF_8.toString()).replaceAll(LINE_BREAK, LINE_BREAK_URL_ENCODED);
    }
}
