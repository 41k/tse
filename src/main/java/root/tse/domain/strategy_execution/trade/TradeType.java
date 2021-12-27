package root.tse.domain.strategy_execution.trade;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import root.tse.domain.order.OrderType;

import static root.tse.domain.order.OrderType.*;

@RequiredArgsConstructor
public enum TradeType {

    LONG(BUY, SELL),
    SHORT(SELL, BUY);

    @Getter
    private final OrderType entryOrderType;
    @Getter
    private final OrderType exitOrderType;
}
