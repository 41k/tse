package root.tse.infrastructure.telegram

import org.springframework.web.client.RestTemplate
import root.tse.configuration.properties.TelegramConfigurationProperties
import spock.lang.Specification

class TelegramApiClientTest extends Specification {

    private properties = Mock(TelegramConfigurationProperties)
    private restTemplate = Mock(RestTemplate)
    private telegramApiClient = new TelegramApiClient(properties, restTemplate)

    def 'should send message'() {
        given:
        def message = '<b>line 1</b>-----<u>line 2</u>-----line 3'
        def urlEncodedMessage = '%3Cb%3Eline+1%3C%2Fb%3E%0A%3Cu%3Eline+2%3C%2Fu%3E%0Aline+3'
        def uri = new URI("http://localhost:8080?message=$urlEncodedMessage")

        when:
        telegramApiClient.sendMessage(message)

        then:
        1 * properties.buildUri(urlEncodedMessage) >> uri
        1 * restTemplate.getForObject(uri, String)
        0 * _
    }
}
