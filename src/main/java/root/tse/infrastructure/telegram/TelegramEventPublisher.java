package root.tse.infrastructure.telegram;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventSubscriber;
import root.tse.domain.strategy_execution.trade.Trade;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.substring;
import static root.tse.infrastructure.telegram.TelegramApiClient.LINE_BREAK;

@RequiredArgsConstructor
public class TelegramEventPublisher implements StrategyExecutionEventSubscriber {

    private static final int TRUNCATED_ID_LENGTH = 8;
    private static final Format DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static final String TRADE_WAS_OPENED_EVENT_FORMAT = "" +
        "<b>Trade was <u>OPENED</u></b>" + LINE_BREAK + LINE_BREAK +
        "<b>Trade Id:</b> %s" + LINE_BREAK +
        "<b>Execution Id:</b> %s" + LINE_BREAK + LINE_BREAK +
        "<b>Symbol:</b> %s" + LINE_BREAK +
        "<b>Amount:</b> %s" + LINE_BREAK +
        "<b>Price:</b> %s" + LINE_BREAK +
        "<b>Total spent:</b> %s" + LINE_BREAK +
        "<b>Opened at:</b> %s";

    private static final String TRADE_WAS_NOT_OPENED_EVENT_FORMAT = "" +
        "<b>Trade was <u>NOT OPENED</u></b>" + LINE_BREAK + LINE_BREAK +
        "<b>Execution Id:</b> %s" + LINE_BREAK +
        "<b>Symbol:</b> %s" + LINE_BREAK +
        "<b>Reason:</b> %s";

    private static final String TRADE_WAS_CLOSED_EVENT_FORMAT = "" +
        "<b>Trade was <u>CLOSED</u></b>" + LINE_BREAK + LINE_BREAK +
        "<b>Trade Id:</b> %s" + LINE_BREAK +
        "<b>Execution Id:</b> %s" + LINE_BREAK + LINE_BREAK +
        "<b>Symbol:</b> %s" + LINE_BREAK +
        "<b>Amount:</b> %s" + LINE_BREAK +
        "<b>Profit:</b> %s" + LINE_BREAK +
        "<b>Opened at:</b> %s" + LINE_BREAK +
        "<b>Closed at:</b> %s";

    private static final String TRADE_WAS_NOT_CLOSED_EVENT_FORMAT = "" +
        "<b>Trade was <u>NOT CLOSED</u></b>" + LINE_BREAK + LINE_BREAK +
        "<b>Trade Id:</b> %s" + LINE_BREAK +
        "<b>Execution Id:</b> %s" + LINE_BREAK +
        "<b>Symbol:</b> %s" + LINE_BREAK +
        "<b>Amount:</b> %s" + LINE_BREAK +
        "<b>Reason:</b> %s";

    private final TelegramApiClient telegramApiClient;

    @Override
    public void acceptTradeWasOpenedEvent(Trade openedTrade) {
        var message = String.format(TRADE_WAS_OPENED_EVENT_FORMAT,
            truncateId(openedTrade.getId()),
            truncateId(openedTrade.getStrategyExecutionId()),
            openedTrade.getSymbol(),
            openedTrade.getAmount(),
            openedTrade.getEntryOrder().getPrice(),
            formTotalSpent(openedTrade),
            formTimeString(openedTrade.getEntryOrder().getTimestamp()));
        telegramApiClient.sendMessage(message);
    }

    @Override
    public void acceptTradeWasNotOpenedEvent(String strategyExecutionId, String symbol, String reason) {
        var message = String.format(TRADE_WAS_NOT_OPENED_EVENT_FORMAT, truncateId(strategyExecutionId), symbol, reason);
        telegramApiClient.sendMessage(message);
    }

    @Override
    public void acceptTradeWasClosedEvent(Trade closedTrade) {
        var message = String.format(TRADE_WAS_CLOSED_EVENT_FORMAT,
            truncateId(closedTrade.getId()),
            truncateId(closedTrade.getStrategyExecutionId()),
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
            truncateId(openedTrade.getId()),
            truncateId(openedTrade.getStrategyExecutionId()),
            openedTrade.getSymbol(),
            openedTrade.getAmount(),
            reason);
        telegramApiClient.sendMessage(message);
    }

    private String truncateId(String id) {
        return substring(id, 0, TRUNCATED_ID_LENGTH);
    }

    private String formTimeString(Long timestamp) {
        return DATE_FORMATTER.format(new Date(timestamp));
    }

    private String formTotalSpent(Trade openedTrade) {
        return String.valueOf(Math.abs(openedTrade.getProfit()));
    }
}
