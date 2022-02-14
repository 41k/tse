package root.tse.infrastructure.persistence.chain_exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.order.OrderType;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "chain_exchange")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChainExchangeDbEntry {
    @Id
    private String id;
    @NotNull
    private String assetChain;
    @NotNull
    private Double orderFeePercent;
    @NotNull
    private Instant executionTimestamp;
    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderExecutionType orderExecutionType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderType order1Type;
    @NotBlank
    private String order1Symbol;
    @NotNull
    private Double order1Amount;
    @NotNull
    private Double order1Price;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderType order2Type;
    @NotBlank
    private String order2Symbol;
    @NotNull
    private Double order2Amount;
    @NotNull
    private Double order2Price;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderType order3Type;
    @NotBlank
    private String order3Symbol;
    @NotNull
    private Double order3Amount;
    @NotNull
    private Double order3Price;

    @NotNull
    private Double profit;
}
