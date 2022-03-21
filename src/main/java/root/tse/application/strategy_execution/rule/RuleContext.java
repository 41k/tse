package root.tse.application.strategy_execution.rule;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.order.OrderType;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Value
@Builder
public class RuleContext {

    String ruleId;
    OrderType orderType;
    Map<RuleParameter, String> parameters;

    public <T> T getParameterValue(RuleParameter parameter, Function<String, T> valueTransformer) {
        return Optional.ofNullable(parameter)
            .map(parameters::get)
            .map(valueTransformer)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Rule parameter %s is not provided", parameter)));
    }
}
