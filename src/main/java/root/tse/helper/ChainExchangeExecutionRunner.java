package root.tse.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import root.tse.application.ChainExchangeExecutionService;
import root.tse.application.model.command.StartChainExchangeExecutionCommand;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;
import root.tse.domain.order.OrderExecutionMode;

//@Component
@RequiredArgsConstructor
public class ChainExchangeExecutionRunner implements CommandLineRunner {

    private final ChainExchangeExecutionService chainExchangeExecutionService;
    private final ExchangeGatewayConfigurationProperties exchangeGatewayConfigurationProperties;

    @Value("${chain-exchange-amount:50}")
    private Double chainExchangeAmount;

    @Value("${min-profit-threshold:-1}")
    private Double minProfitThreshold;

    @Override
    public void run(String... args) {
        exchangeGatewayConfigurationProperties.getAssetChains().keySet().forEach(assetChainId -> {
            var command = StartChainExchangeExecutionCommand.builder()
                .assetChainId(assetChainId)
                .amount(chainExchangeAmount)
                .minProfitThreshold(minProfitThreshold)
                .orderExecutionMode(OrderExecutionMode.STUB)
                .build();
            chainExchangeExecutionService.handle(command);
        });
    }

//    @Override
//    public void run(String... args) {
//        var command = StartChainExchangeExecutionCommand.builder()
//            .assetChainId(1)
//            .amount(chainExchangeAmount)
//            .minProfitThreshold(minProfitThreshold)
//            .orderExecutionMode(OrderExecutionMode.STUB)
//            .build();
//        chainExchangeExecutionService.handle(command);
//    }
}