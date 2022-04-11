package root.tse.application.rule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RuleParameter<T> {

    public static final RuleParameter<Double> PROFIT_VALUE = new RuleParameter<>("profit-value", Double::valueOf);
    public static final RuleParameter<Double> LOSS_VALUE = new RuleParameter<>("loss-value", Double::valueOf);
    public static final RuleParameter<String> COMPARISON_OPERATOR = new RuleParameter<>("comparison-operator", String::valueOf);
    public static final RuleParameter<Double> TARGET_PRICE = new RuleParameter<>("target-price", Double::valueOf);

    @Getter
    private final String name;
    private final Function<String, T> valueTransformer;

    public T transformValue(String valueAsString) {
        return valueTransformer.apply(valueAsString);
    }
}
