package root.tse.application.rule;

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
