package root.tse.infrastructure.chain_exchange_execution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import root.tse.domain.chain_exchange_execution.ChainExchangeExecution;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ChainExchangeTask {

    private final Map<Integer, ChainExchangeExecution> chainExchangeExecutionStore;

    @Scheduled(fixedDelayString = "${exchange-gateway.chain-exchange-run-interval-in-millis}")
    public void run() {
        chainExchangeExecutionStore.values().forEach(chainExchangeExecution -> {
            try {
                chainExchangeExecution.run();
            } catch (Exception e) {
                log.error(">>> error during chain exchange execution {}",
                    chainExchangeExecution.getContext().getAssetChain(), e);
            }
        });
    }
}
