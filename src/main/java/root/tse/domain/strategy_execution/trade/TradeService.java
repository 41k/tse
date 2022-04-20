package root.tse.domain.strategy_execution.trade;

import lombok.RequiredArgsConstructor;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.IdGenerator;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.order.Order;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TradeService {

    private final IdGenerator idGenerator;
    private final ExchangeGateway exchangeGateway;
    private final TradeRepository tradeRepository;

    public Optional<Trade> tryToOpenTrade(TradeOpeningContext context) {
        var symbol = context.getSymbol();
        var tradeType = context.getTradeType();
        var orderType = tradeType.getEntryOrderType();
        return exchangeGateway.getCurrentPrices(List.of(symbol))
            .map(currentPrices -> {
                var fundsPerTrade = context.getFundsPerTrade();
                var price = currentPrices.get(symbol).get(orderType);
                return Order.builder()
                    .type(orderType)
                    .executionType(context.getOrderExecutionType())
                    .symbol(symbol)
                    .amount(fundsPerTrade / price)
                    .timestamp(context.getClockSignal().getTimestamp())
                    .build();
            })
            .flatMap(exchangeGateway::tryToExecute)
            .map(executedEntryOrder -> {
                var openedTrade = Trade.builder()
                    .id(idGenerator.generate())
                    .strategyExecutionId(context.getStrategyExecutionId())
                    .type(tradeType)
                    .orderFeePercent(exchangeGateway.getOrderFeePercent())
                    .entryOrder(executedEntryOrder)
                    .build();
                tradeRepository.save(openedTrade);
                return openedTrade;
            });
    }

    public Optional<Trade> tryToCloseTrade(Trade openedTrade, ClockSignal clockSignal) {
        var timestamp = clockSignal.getTimestamp();
        var exitOrder = openedTrade.formExitOrder(timestamp);
        return exchangeGateway.tryToExecute(exitOrder)
            .map(executedExitOrder -> {
                var closedTrade = openedTrade.toBuilder().exitOrder(executedExitOrder).build();
                tradeRepository.save(closedTrade);
                return closedTrade;
            });
    }

    public Collection<Trade> getAllTradesByStrategyExecutionId(String strategyExecutionId) {
        return tradeRepository.getAllTradesByStrategyExecutionId(strategyExecutionId);
    }
}
