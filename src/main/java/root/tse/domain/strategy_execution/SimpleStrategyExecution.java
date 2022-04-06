package root.tse.domain.strategy_execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.clock.Interval;
import root.tse.domain.event.EventBus;
import root.tse.domain.strategy_execution.trade.Trade;
import root.tse.domain.strategy_execution.trade.TradeOpeningContext;
import root.tse.domain.strategy_execution.trade.TradeService;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@RequiredArgsConstructor
public class SimpleStrategyExecution implements StrategyExecution {

    @Getter
    private final String id;
    @Getter
    private final StrategyExecutionContext context;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final TradeService tradeService;
    private final EventBus eventBus;

    private Trade openedTrade;

    @Override
    public void start() {
        clockSignalDispatcher.subscribe(clockSignalIntervals(), this);
    }

    @Override
    public void stop() {
        clockSignalDispatcher.unsubscribe(clockSignalIntervals(), this);
    }

    @Override
    public synchronized void accept(ClockSignal clockSignal) {
        if (openedTrade == null) {
            tryToOpenTrade(clockSignal);
        } else {
            tryToCloseTrade(clockSignal);
        }
    }

    private void tryToOpenTrade(ClockSignal clockSignal) {
        var entryRule = context.getEntryRule();
        var symbol = context.getSymbols().get(0);
        if (entryRule.isSatisfied(clockSignal, symbol)) {
            var tradeOpeningContext = TradeOpeningContext.builder()
                .strategyExecutionId(id)
                .orderExecutionType(context.getOrderExecutionType())
                .tradeType(context.getTradeType())
                .clockSignal(clockSignal)
                .symbol(symbol)
                .fundsPerTrade(context.getFundsPerTrade())
                .build();
            tradeService.tryToOpenTrade(tradeOpeningContext)
                .ifPresentOrElse(
                    openedTrade -> {
                        this.openedTrade = openedTrade;
                        eventBus.publishTradeWasOpenedEvent(openedTrade);
                    },
                    () -> eventBus.publishTradeWasNotOpenedEvent(id, symbol, EMPTY));
        }
    }

    private void tryToCloseTrade(ClockSignal clockSignal) {
        var exitRule = context.getExitRule();
        var entryOrder = openedTrade.getEntryOrder();
        if (exitRule.isSatisfied(clockSignal, entryOrder)) {
            tradeService.tryToCloseTrade(openedTrade, clockSignal)
                .ifPresentOrElse(
                    closedTrade -> {
                        this.openedTrade = null;
                        eventBus.publishTradeWasClosedEvent(closedTrade);
                    },
                    () -> eventBus.publishTradeWasNotClosedEvent(openedTrade, EMPTY));
        }
    }

    private Set<Interval> clockSignalIntervals() {
        return Set.of(
            context.getEntryRule().getCheckInterval(),
            context.getExitRule().getCheckInterval()
        );
    }
}
