package root.tse.infrastructure.persistence.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.tse.domain.strategy_execution.trade.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "trade")
@Data
@Builder
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
    private String symbol;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderType entryOrderType;
    @NotNull
    private Double entryOrderAmount;
    @NotNull
    private Double entryOrderPrice;
    @NotNull
    private Instant entryOrderTimestamp;
    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderStatus entryOrderStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderType exitOrderType;
    @NotNull
    private Double exitOrderAmount;
    @NotNull
    private Double exitOrderPrice;
    @NotNull
    private Instant exitOrderTimestamp;
    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderStatus exitOrderStatus;
}
