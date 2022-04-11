package root.tse.application.rule;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.order.OrderType;

import java.util.Map;
import java.util.Optional;

@Value
@Builder
public class RuleContext {

    String ruleId;
    OrderType orderType;
    Map<String, String> parameters;

    public <T> T getParameterValue(RuleParameter<T> parameter) {
        return Optional.ofNullable(parameters)
            .map(parameters -> parameters.get(parameter.getName()))
            .map(parameter::transformValue)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Rule parameter [%s] is not provided", parameter.getName())));
    }
}
