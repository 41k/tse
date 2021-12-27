package root.tse.domain.strategy_execution.trade;

import lombok.RequiredArgsConstructor;
import root.tse.domain.IdGenerator;
import root.tse.domain.order.OrderExecutor;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
public class TradeService {

    private final IdGenerator idGenerator;
    private final OrderExecutor orderExecutor;
    private final TradeRepository tradeRepository;

    public Optional<Trade> tryToOpenTrade(TradeOpeningContext context) {
        var entryOrder = context.getEntryOrder();
        var executionMode = context.getOrderExecutionMode();
        var executedEntryOrder = orderExecutor.execute(entryOrder, executionMode);
        if (executedEntryOrder.wasNotFilled()) {
            return Optional.empty();
        }
        var openedTrade = Trade.builder()
            .id(idGenerator.generateId())
            .strategyExecutionId(context.getStrategyExecutionId())
            .type(context.getTradeType())
            .orderFeePercent(context.getOrderFeePercent())
            .entryOrderClockSignal(context.getEntryOrderClockSignal())
            .entryOrder(executedEntryOrder)
            .build();
        tradeRepository.save(openedTrade);
        return Optional.of(openedTrade);
    }

    public Optional<Trade> tryToCloseTrade(TradeClosingContext context) {
        var exitOrder = context.getExitOrder();
        var executionMode = context.getOrderExecutionMode();
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
