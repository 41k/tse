package root.tse.infrastructure.exchange_gateway;

import lombok.Data;

import java.util.List;

@Data
public class OrderExecutionResult {

    private String symbol;
    private String orderId;
    private Long transactTime;
    private String price;
    private String origQty;
    private String executedQty;
    private String status;
    private String timeInForce;
    private String type;
    private String side;
    private List<Fill> fills;

    @Data
    public static class Fill {
        private String price;
        private String qty;
        private String commission;
        private String commissionAsset;
    }
}
