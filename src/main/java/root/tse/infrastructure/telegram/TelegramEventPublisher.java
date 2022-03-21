package root.tse.infrastructure.telegram;

import lombok.RequiredArgsConstructor;
import root.tse.domain.chain_exchange_execution.ChainExchange;
import root.tse.domain.event.EventSubscriber;
import root.tse.domain.strategy_execution.trade.Trade;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import static root.tse.infrastructure.telegram.TelegramApiClient.LINE_BREAK;

@RequiredArgsConstructor
public class TelegramEventPublisher implements EventSubscriber {

    private static final Format DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final String TRADE_WAS_OPENED_EVENT_FORMAT = "" +
        "<b>Trade was <u>OPENED</u></b>" + LINE_BREAK + LINE_BREAK +
        "<b>Trade:</b> %s" + LINE_BREAK +
        "<b>Strategy execution:</b> %s" + LINE_BREAK + LINE_BREAK +
        "<b>Order execution type:</b> %s" + LINE_BREAK +
        "<b>Symbol:</b> %s" + LINE_BREAK +
        "<b>Amount:</b> %s" + LINE_BREAK +
        "<b>Price:</b> %s" + LINE_BREAK +
        "<b>Total spent:</b> %s" + LINE_BREAK +
        "<b>Opened at:</b> %s";

    private static final String TRADE_WAS_NOT_OPENED_EVENT_FORMAT = "" +
        "<b>Trade was <u>NOT OPENED</u></b>" + LINE_BREAK + LINE_BREAK +
        "<b>Strategy execution:</b> %s" + LINE_BREAK +
        "<b>Symbol:</b> %s" + LINE_BREAK +
        "<b>Reason:</b> %s";

    private static final String TRADE_WAS_CLOSED_EVENT_FORMAT = "" +
        "<b>Trade was <u>CLOSED</u></b>" + LINE_BREAK + LINE_BREAK +
        "<b>Trade:</b> %s" + LINE_BREAK +
        "<b>Strategy execution:</b> %s" + LINE_BREAK + LINE_BREAK +
        "<b>Order execution type:</b> %s" + LINE_BREAK +
        "<b>Symbol:</b> %s" + LINE_BREAK +
        "<b>Amount:</b> %s" + LINE_BREAK +
        "<b>Profit:</b> %s" + LINE_BREAK +
        "<b>Opened at:</b> %s" + LINE_BREAK +
        "<b>Closed at:</b> %s";

    private static final String TRADE_WAS_NOT_CLOSED_EVENT_FORMAT = "" +
        "<b>Trade was <u>NOT CLOSED</u></b>" + LINE_BREAK + LINE_BREAK +
        "<b>Trade:</b> %s" + LINE_BREAK +
        "<b>Strategy execution:</b> %s" + LINE_BREAK +
        "<b>Order execution type:</b> %s" + LINE_BREAK +
        "<b>Symbol:</b> %s" + LINE_BREAK +
        "<b>Amount:</b> %s" + LINE_BREAK +
        "<b>Reason:</b> %s";

    private static final String CHAIN_EXCHANGE_WAS_EXECUTED_EVENT_FORMAT = "" +
        "<b>Chain exchange was <u>EXECUTED</u></b>" + LINE_BREAK + LINE_BREAK +
        "<b>Chain exchange:</b> %s" + LINE_BREAK +
        "<b>Asset chain:</b> %s" + LINE_BREAK +
        "<b>Profit:</b> %s";

    private static final String CHAIN_EXCHANGE_EXECUTION_FAILED_EVENT_FORMAT = "" +
        "<b>Chain exchange execution <u>FAILED</u></b>" + LINE_BREAK + LINE_BREAK +
        "<b>Chain exchange:</b> %s" + LINE_BREAK +
        "<b>Asset chain:</b> %s";

    private final TelegramApiClient telegramApiClient;

    @Override
    public void acceptTradeWasOpenedEvent(Trade openedTrade) {
        var message = String.format(TRADE_WAS_OPENED_EVENT_FORMAT,
            openedTrade.getId(),
            openedTrade.getStrategyExecutionId(),
            openedTrade.getEntryOrder().getExecutionType(),
            openedTrade.getSymbol(),
            openedTrade.getAmount(),
            openedTrade.getEntryOrder().getPrice(),
            formTotalSpent(openedTrade),
            formTimeString(openedTrade.getEntryOrder().getTimestamp()));
        telegramApiClient.sendMessage(message);
    }

    @Override
    public void acceptTradeWasNotOpenedEvent(String strategyExecutionId, String symbol, String reason) {
        var message = String.format(TRADE_WAS_NOT_OPENED_EVENT_FORMAT, strategyExecutionId, symbol, reason);
        telegramApiClient.sendMessage(message);
    }

    @Override
    public void acceptTradeWasClosedEvent(Trade closedTrade) {
        var message = String.format(TRADE_WAS_CLOSED_EVENT_FORMAT,
            closedTrade.getId(),
            closedTrade.getStrategyExecutionId(),
            closedTrade.getEntryOrder().getExecutionType(),
            closedTrade.getSymbol(),
            closedTrade.getAmount(),
            closedTrade.getProfit(),
            formTimeString(closedTrade.getEntryOrder().getTimestamp()),
            formTimeString(closedTrade.getExitOrder().getTimestamp()));
        telegramApiClient.sendMessage(message);
    }

    @Override
    public void acceptTradeWasNotClosedEvent(Trade openedTrade, String reason) {
        var message = String.format(TRADE_WAS_NOT_CLOSED_EVENT_FORMAT,
            openedTrade.getId(),
            openedTrade.getStrategyExecutionId(),
            openedTrade.getEntryOrder().getExecutionType(),
            openedTrade.getSymbol(),
            openedTrade.getAmount(),
            reason);
        telegramApiClient.sendMessage(message);
    }

    @Override
    public void acceptChainExchangeWasExecutedEvent(ChainExchange chainExchange) {
        var message = String.format(CHAIN_EXCHANGE_WAS_EXECUTED_EVENT_FORMAT,
            chainExchange.getId(),
            chainExchange.getAssetChain(),
            chainExchange.getProfit());
        telegramApiClient.sendMessage(message);
    }

    @Override
    public void acceptChainExchangeExecutionFailedEvent(String chainExchangeId, String assetChain) {
        var message = String.format(CHAIN_EXCHANGE_EXECUTION_FAILED_EVENT_FORMAT, chainExchangeId, assetChain);
        telegramApiClient.sendMessage(message);
    }

    private String formTimeString(Long timestamp) {
        return DATE_FORMATTER.format(new Date(timestamp));
    }

    private String formTotalSpent(Trade openedTrade) {
        return String.valueOf(Math.abs(openedTrade.getProfit()));
    }
}
