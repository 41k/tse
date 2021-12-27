package root.tse.infrastructure.exchange_gateway;

import lombok.Data;

import java.util.Map;

@Data
public class WssIncomingMessage {
    String status;
    String destination;
    Map<String, Object> payload;
}
