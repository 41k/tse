package root.tse.domain.strategy_execution.trade;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static root.tse.domain.strategy_execution.trade.OrderType.*;

@RequiredArgsConstructor
public enum TradeType {

    LONG(BUY, SELL),
    SHORT(SELL, BUY);

    @Getter
    private final OrderType entryOrderType;
    @Getter
    private final OrderType exitOrderType;
}
