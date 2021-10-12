package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.strategy_execution.StrategyExecution;
import root.tse.domain.strategy_execution.clock.ClockSignalConsumer;
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher;
import root.tse.domain.strategy_execution.rule.ExitRule;

@Slf4j
@Builder
public class TradeExecution implements ClockSignalConsumer {

    private final Trade openedTrade;
    private final ExitRule exitRule;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final StrategyExecution strategyExecution;

    @Override
    public String getId() {
        return openedTrade.getId();
    }

    public void start() {
        var interval = exitRule.getLowestInterval();
        clockSignalDispatcher.subscribe(interval, this);
    }

    public void stop() {
        var interval = exitRule.getLowestInterval();
        clockSignalDispatcher.unsubscribe(interval, this);
    }

    @Override
    public void acceptClockSignal() {
        var symbol = openedTrade.getSymbol();
        var entryOrder = openedTrade.getEntryOrder();
        var ruleCheckResult = exitRule.check(entryOrder);
        if (ruleCheckResult.ruleWasSatisfied()) {
            var bar = ruleCheckResult.getBarOnWhichRuleWasSatisfied();
            var exitOrder = Order.builder()
                .type(openedTrade.getExitOrderType())
                .symbol(symbol)
                .amount(openedTrade.getAmount())
                .price(bar.getClosePrice().doubleValue())
                .timestamp(bar.getEndTime().toInstant().toEpochMilli())
                .build();
            var tradeToClose = openedTrade.toBuilder().exitOrder(exitOrder).build();
            strategyExecution.closeTrade(tradeToClose);
        }
    }
}
