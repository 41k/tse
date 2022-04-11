package root.tse.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import root.tse.application.chain_exchange_execution.ChainExchangeExecutionService;
import root.tse.application.chain_exchange_execution.StartChainExchangeExecutionCommand;
import root.tse.application.chain_exchange_execution.StopChainExchangeExecutionCommand;
import root.tse.presentation.dto.ChainExchangeExecutionDto;
import root.tse.presentation.dto.StartChainExchangeExecutionRequest;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chain-exchange-executions")
@RequiredArgsConstructor
public class ChainExchangeExecutionController {

    private final ChainExchangeExecutionService chainExchangeExecutionService;

    @PostMapping
    public void startChainExchangeExecution(@RequestBody @Valid StartChainExchangeExecutionRequest request) {
        var command = StartChainExchangeExecutionCommand.builder()
            .assetChainId(request.getAssetChainId())
            .orderExecutionType(request.getOrderExecutionType())
            .amount(request.getAmount())
            .minProfitThreshold(request.getMinProfitThreshold())
            .build();
        chainExchangeExecutionService.handle(command);
    }

    @DeleteMapping("/{assetChainId}")
    public void stopChainExchangeExecution(@PathVariable Integer assetChainId) {
        var command = new StopChainExchangeExecutionCommand(assetChainId);
        chainExchangeExecutionService.handle(command);
    }

    @GetMapping
    public Collection<ChainExchangeExecutionDto> getChainExchangeExecutions() {
        return chainExchangeExecutionService.getChainExchangeExecutions().stream()
            .map(ChainExchangeExecutionDto::from)
            .collect(Collectors.toList());
    }

    @GetMapping("/asset-chains")
    public Map<Integer, List<String>> getAssetChains() {
        return chainExchangeExecutionService.getAssetChains();
    }
}
