package root.tse.infrastructure.telegram

import spock.lang.Specification

import static root.tse.domain.order.OrderType.BUY
import static root.tse.util.TestUtils.*

class TelegramEventPublisherTest extends Specification {

    private telegramApiClient = Mock(TelegramApiClient)
    private telegramEventPublisher = new TelegramEventPublisher(telegramApiClient)

    def 'should publish TRADE WAS OPENED event'() {
        when:
        telegramEventPublisher.acceptTradeWasOpenedEvent(OPENED_TRADE)

        then:
        1 * telegramApiClient.sendMessage(
            "<b>Trade was <u>OPENED</u></b>----------" +
            "<b>Trade:</b> $TRADE_ID-----" +
            "<b>Strategy execution:</b> $STRATEGY_EXECUTION_ID----------" +
            "<b>Order execution type:</b> $ORDER_EXECUTION_TYPE-----" +
            "<b>Symbol:</b> $SYMBOL_1-----" +
            "<b>Amount:</b> $AMOUNT_1-----" +
            "<b>Price:</b> $PRICE_1-----" +
            "<b>Total spent:</b> 200.4-----" +
            "<b>Opened at:</b> 2021-10-03 04:00"
        )
        0 * _
    }

    def 'should publish TRADE WAS NOT OPENED event'() {
        when:
        telegramEventPublisher.acceptTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, REASON)

        then:
        1 * telegramApiClient.sendMessage(
            "<b>Trade was <u>NOT OPENED</u></b>----------" +
            "<b>Strategy execution:</b> $STRATEGY_EXECUTION_ID-----" +
            "<b>Symbol:</b> $SYMBOL_1-----" +
            "<b>Reason:</b> $REASON"
        )
        0 * _
    }

    def 'should publish TRADE WAS CLOSED event'() {
        when:
        telegramEventPublisher.acceptTradeWasClosedEvent(CLOSED_TRADE)

        then:
        1 * telegramApiClient.sendMessage(
            "<b>Trade was <u>CLOSED</u></b>----------" +
            "<b>Trade:</b> $TRADE_ID-----" +
            "<b>Strategy execution:</b> $STRATEGY_EXECUTION_ID----------" +
            "<b>Order execution type:</b> $ORDER_EXECUTION_TYPE-----" +
            "<b>Symbol:</b> $SYMBOL_1-----" +
            "<b>Amount:</b> $AMOUNT_1-----" +
            "<b>Profit:</b> 1895.4-----" +
            "<b>Opened at:</b> 2021-10-03 04:00-----" +
            "<b>Closed at:</b> 2021-10-04 04:00"
        )
        0 * _
    }

    def 'should publish TRADE WAS NOT CLOSED event'() {
        when:
        telegramEventPublisher.acceptTradeWasNotClosedEvent(OPENED_TRADE, REASON)

        then:
        1 * telegramApiClient.sendMessage(
            "<b>Trade was <u>NOT CLOSED</u></b>----------" +
            "<b>Trade:</b> $TRADE_ID-----" +
            "<b>Strategy execution:</b> $STRATEGY_EXECUTION_ID-----" +
            "<b>Order execution type:</b> $ORDER_EXECUTION_TYPE-----" +
            "<b>Symbol:</b> $SYMBOL_1-----" +
            "<b>Amount:</b> $AMOUNT_1-----" +
            "<b>Reason:</b> $REASON"
        )
        0 * _
    }

    def 'should publish CHAIN EXCHANGE WAS EXECUTED event'() {
        when:
        telegramEventPublisher.acceptChainExchangeWasExecutedEvent(EXECUTED_CHAIN_EXCHANGE, ASSET_CHAIN)

        then:
        1 * telegramApiClient.sendMessage(
            "<b>Chain exchange was <u>EXECUTED</u></b>----------" +
            "<b>Chain exchange:</b> $CHAIN_EXCHANGE_ID-----" +
            "<b>Asset chain:</b> $ASSET_CHAIN-----" +
            "<b>Profit:</b> $CHAIN_EXCHANGE_PROFIT"
        )
        0 * _
    }

    def 'should publish CHAIN EXCHANGE EXECUTION FAILED event'() {
        when:
        telegramEventPublisher.acceptChainExchangeExecutionFailedEvent(CHAIN_EXCHANGE_ID, ASSET_CHAIN)

        then:
        1 * telegramApiClient.sendMessage(
            "<b>Chain exchange execution <u>FAILED</u></b>----------" +
            "<b>Chain exchange:</b> $CHAIN_EXCHANGE_ID-----" +
            "<b>Asset chain:</b> $ASSET_CHAIN"
        )
        0 * _
    }

    def 'should publish ORDER WAS EXECUTED event'() {
        when:
        telegramEventPublisher.acceptOrderWasExecutedEvent(ORDER_EXECUTION_ID, ENTRY_ORDER)

        then:
        1 * telegramApiClient.sendMessage(
            "<b>Order was <u>EXECUTED</u></b>----------" +
            "<b>Order execution:</b> $ORDER_EXECUTION_ID-----" +
            "<b>Order type:</b> $BUY-----" +
            "<b>Order execution type:</b> $ORDER_EXECUTION_TYPE-----" +
            "<b>Symbol:</b> $SYMBOL_1-----" +
            "<b>Amount:</b> $AMOUNT_1-----" +
            "<b>Price:</b> $PRICE_1-----" +
            "<b>Executed at:</b> 2021-10-03 04:00"
        )
        0 * _
    }

    def 'should publish ORDER EXECUTION FAILED event'() {
        when:
        telegramEventPublisher.acceptOrderExecutionFailedEvent(ORDER_EXECUTION_ID, BUY, SYMBOL_1)

        then:
        1 * telegramApiClient.sendMessage(
            "<b>Order execution <u>FAILED</u></b>----------" +
            "<b>Order execution:</b> $ORDER_EXECUTION_ID-----" +
            "<b>Order type:</b> $BUY-----" +
            "<b>Symbol:</b> $SYMBOL_1"
        )
        0 * _
    }
}
