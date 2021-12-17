package root.tse.configuration.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import java.net.URI;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ConfigurationProperties(prefix = "telegram")
public class TelegramConfigurationProperties {
    @NotBlank
    private String urlFormat;
    @NotBlank
    private String token;
    @NotBlank
    private String chatId;
    private boolean enabled;

    @SneakyThrows
    public URI buildUri(String message) {
        return new URI(String.format(urlFormat, token, chatId, message));
    }
}
