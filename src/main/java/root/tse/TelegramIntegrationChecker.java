package root.tse;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventSubscriber;
import root.tse.domain.strategy_execution.trade.Order;
import root.tse.domain.strategy_execution.trade.Trade;
import root.tse.domain.strategy_execution.trade.TradeType;

import java.util.UUID;

//@Component
@RequiredArgsConstructor
public class TelegramIntegrationChecker implements CommandLineRunner {

    private final StrategyExecutionEventSubscriber telegramEventPublisher;

    @Override
    public void run(String... args) {
        var tradeId = UUID.randomUUID().toString();
        var strategyExecutionId = UUID.randomUUID().toString();
        var symbol = "ETH/USD";
        var openedTrade = Trade.builder()
            .id(tradeId)
            .strategyExecutionId(strategyExecutionId)
            .type(TradeType.LONG)
            .transactionFeePercent(0.2d)
            .entryOrder(Order.builder()
                .symbol(symbol)
                .amount(0.5d)
                .price(3660d)
                .timestamp(1639737000000L)
                .build())
            .build();
        var closedTrade = openedTrade.toBuilder()
            .exitOrder(Order.builder()
                .symbol(symbol)
                .amount(0.5d)
                .price(4000d)
                .timestamp(1639746000000L)
                .build())
            .build();

        telegramEventPublisher.acceptTradeWasOpenedEvent(openedTrade);
        telegramEventPublisher.acceptTradeWasNotOpenedEvent(strategyExecutionId, symbol, "Another trade for the same symbol.");
        telegramEventPublisher.acceptTradeWasClosedEvent(closedTrade);
        telegramEventPublisher.acceptTradeWasNotClosedEvent(openedTrade, "Exchange gateway processing error");
    }
}
