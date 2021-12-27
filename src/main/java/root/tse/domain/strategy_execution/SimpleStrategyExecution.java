package root.tse.domain.strategy_execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.clock.Interval;
import root.tse.domain.event.EventBus;
import root.tse.domain.strategy_execution.trade.Trade;
import root.tse.domain.strategy_execution.trade.TradeClosingContext;
import root.tse.domain.strategy_execution.trade.TradeOpeningContext;
import root.tse.domain.strategy_execution.trade.TradeService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
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
        var clockSignalIntervals = getClockSignalIntervals();
        clockSignalDispatcher.subscribe(clockSignalIntervals, this);
    }

    @Override
    public void stop() {
        var clockSignalIntervals = getClockSignalIntervals();
        clockSignalDispatcher.unsubscribe(clockSignalIntervals, this);
    }

    @Override
    public synchronized void accept(ClockSignal clockSignal) {
        if (tradeCanBeOpened()) {
            tryToOpenTrade(clockSignal);
        } else if (tradeCanBeClosed(clockSignal)) {
            tryToCloseTrade(clockSignal);
        }
    }

    private void tryToOpenTrade(ClockSignal clockSignal) {
        var entryRule = context.getEntryRule();
        var symbol = context.getSymbols().get(0);
        var ruleCheckResult = entryRule.check(clockSignal, symbol);
        if (ruleCheckResult.ruleWasSatisfied()) {
            var tradeOpeningContext = TradeOpeningContext.builder()
                .strategyExecutionId(id)
                .orderExecutionMode(context.getOrderExecutionMode())
                .tradeType(context.getTradeType())
                .entryOrderClockSignal(clockSignal)
                .symbol(symbol)
                .bar(ruleCheckResult.getBarOnWhichRuleWasSatisfied())
                .fundsPerTrade(context.getFundsPerTrade())
                .orderFeePercent(context.getOrderFeePercent())
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
        var ruleCheckResult = exitRule.check(clockSignal, entryOrder);
        if (ruleCheckResult.ruleWasSatisfied()) {
            var tradeClosingContext = TradeClosingContext.builder()
                .openedTrade(openedTrade)
                .bar(ruleCheckResult.getBarOnWhichRuleWasSatisfied())
                .orderExecutionMode(context.getOrderExecutionMode())
                .build();
            tradeService.tryToCloseTrade(tradeClosingContext)
                .ifPresentOrElse(
                    closedTrade -> {
                        this.openedTrade = null;
                        eventBus.publishTradeWasClosedEvent(closedTrade);
                    },
                    () -> eventBus.publishTradeWasNotClosedEvent(openedTrade, EMPTY));
        }
    }

    private boolean tradeCanBeOpened() {
        return isNull(openedTrade);
    }

    private boolean tradeCanBeClosed(ClockSignal clockSignal) {
        return nonNull(openedTrade) && openedTrade.getEntryOrderClockSignal().isBefore(clockSignal);
    }

    private Set<Interval> getClockSignalIntervals() {
        return new HashSet<>(List.of(
            context.getEntryRule().getLowestInterval(),
            context.getExitRule().getLowestInterval()
        ));
    }
}
