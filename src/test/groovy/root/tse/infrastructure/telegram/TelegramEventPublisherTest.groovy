package root.tse.infrastructure.telegram

import spock.lang.Specification

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
            "<b>Trade Id:</b> 34598437-----" +
            "<b>Execution Id:</b> 3545de05----------" +
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
            "<b>Execution Id:</b> 3545de05-----" +
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
            "<b>Trade Id:</b> 34598437-----" +
            "<b>Execution Id:</b> 3545de05----------" +
            "<b>Symbol:</b> $SYMBOL_1-----" +
            "<b>Amount:</b> $AMOUNT_1-----" +
            "<b>Profit:</b> 1895.3999999999999-----" +
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
            "<b>Trade Id:</b> 34598437-----" +
            "<b>Execution Id:</b> 3545de05-----" +
            "<b>Symbol:</b> $SYMBOL_1-----" +
            "<b>Amount:</b> $AMOUNT_1-----" +
            "<b>Reason:</b> $REASON"
        )
        0 * _
    }
}
