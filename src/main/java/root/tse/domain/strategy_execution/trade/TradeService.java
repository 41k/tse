package root.tse.domain.strategy_execution.trade;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class TradeService {

    private final OrderExecutor orderExecutor;
    private final TradeRepository tradeRepository;

    public Optional<Trade> tryToOpenTrade(TradeOpeningContext context) {
        var entryOrder = context.getEntryOrder();
        var executionMode = context.getStrategyExecutionMode();
        var executedEntryOrder = orderExecutor.execute(entryOrder, executionMode);
        if (executedEntryOrder.wasNotFilled()) {
            return Optional.empty();
        }
        var openedTrade = Trade.builder()
            .id(UUID.randomUUID().toString())
            .strategyExecutionId(context.getStrategyExecutionId())
            .type(context.getTradeType())
            .entryOrderClockSignal(context.getEntryOrderClockSignal())
            .entryOrder(executedEntryOrder)
            .build();
        tradeRepository.save(openedTrade);
        return Optional.of(openedTrade);
    }

    public Optional<Trade> tryToCloseTrade(TradeClosingContext context) {
        var exitOrder = context.getExitOrder();
        var executionMode = context.getStrategyExecutionMode();
        var executedExitOrder = orderExecutor.execute(exitOrder, executionMode);
        if (executedExitOrder.wasNotFilled()) {
            return Optional.empty();
        }
        var openedTrade = context.getOpenedTrade();
        var closedTrade = openedTrade.toBuilder().exitOrder(executedExitOrder).build();
        tradeRepository.save(closedTrade);
        return Optional.of(closedTrade);
    }

    public Collection<Trade> getAllTradesByStrategyExecutionId(String strategyExecutionId) {
        return tradeRepository.getAllTradesByStrategyExecutionId(strategyExecutionId);
    }
}
