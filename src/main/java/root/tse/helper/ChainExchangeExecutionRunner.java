package root.tse.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import root.tse.application.chain_exchange_execution.ChainExchangeExecutionService;
import root.tse.application.chain_exchange_execution.StartChainExchangeExecutionCommand;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;
import root.tse.domain.order.OrderExecutionType;

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
                .orderExecutionType(OrderExecutionType.STUB)
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
//            .orderExecutionType(OrderExecutionType.STUB)
//            .build();
//        chainExchangeExecutionService.handle(command);
//    }
}
