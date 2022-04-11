package root.tse.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import root.tse.application.order_execution.OrderExecutionService;
import root.tse.application.order_execution.StartOrderExecutionCommand;
import root.tse.application.order_execution.StopOrderExecutionCommand;
import root.tse.presentation.dto.OrderExecutionDto;
import root.tse.presentation.dto.StartOrderExecutionRequest;

import javax.validation.Valid;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/order-executions")
@RequiredArgsConstructor
public class OrderExecutionController {

    private final OrderExecutionService orderExecutionService;

    @PostMapping
    public void startOrderExecution(@RequestBody @Valid StartOrderExecutionRequest request) {
        var command = StartOrderExecutionCommand.builder()
            .orderType(request.getOrderType())
            .orderExecutionType(request.getOrderExecutionType())
            .symbol(request.getSymbol())
            .amount(request.getAmount())
            .ruleId(request.getRuleId())
            .ruleParameters(request.getRuleParameters())
            .build();
        orderExecutionService.handle(command);
    }

    @DeleteMapping("/{orderExecutionId}")
    public void stopOrderExecution(@PathVariable String orderExecutionId) {
        var command = new StopOrderExecutionCommand(orderExecutionId);
        orderExecutionService.handle(command);
    }

    @GetMapping
    public Collection<OrderExecutionDto> getStrategyExecutions() {
        return orderExecutionService.getOrderExecutions().stream()
            .map(OrderExecutionDto::from)
            .collect(Collectors.toList());
    }
}
