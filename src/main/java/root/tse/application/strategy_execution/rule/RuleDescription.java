package root.tse.application.strategy_execution.rule;

import lombok.Builder;
import lombok.Value;

import java.util.Collection;

@Value
@Builder
public class RuleDescription {
    String id;
    String name;
    Collection<RuleParameter> parameters;
}
