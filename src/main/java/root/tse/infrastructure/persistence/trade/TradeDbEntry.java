package root.tse.infrastructure.persistence.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.order.OrderType;
import root.tse.domain.strategy_execution.trade.TradeType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "trade")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TradeDbEntry {
    @Id
    private String id;
    @NotNull
    private String strategyExecutionId;
    @NotNull
    @Enumerated(EnumType.STRING)
    private TradeType type;
    @NotNull
    private Double orderFeePercent;
    @NotNull
    private String symbol;
    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderExecutionType orderExecutionType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderType entryOrderType;
    @NotNull
    private Double entryOrderAmount;
    @NotNull
    private Double entryOrderPrice;
    @NotNull
    private Instant entryOrderTimestamp;

    @Enumerated(EnumType.STRING)
    private OrderType exitOrderType;
    private Double exitOrderAmount;
    private Double exitOrderPrice;
    private Instant exitOrderTimestamp;
}
