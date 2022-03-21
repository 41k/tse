package root.tse.application.strategy_execution.rule;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class Comparison {

    private static final Map<String, Function<Double, Function<Double, Boolean>>> RESULT_PROVIDER_BUILDERS = Map.of(
        ">", (Double operand2) -> (Double operand1) -> operand1 > operand2,
        ">=", (Double operand2) -> (Double operand1) -> operand1 >= operand2,
        "<", (Double operand2) -> (Double operand1) -> operand1 < operand2,
        "<=", (Double operand2) -> (Double operand1) -> operand1 <= operand2,
        "=", (Double operand2) -> (Double operand1) -> operand1.equals(operand2)
    );

    @Getter
    private final String description;
    private final Function<Double, Boolean> resultProvider;

    public Comparison(String operator, Double operand2) {
        this.description = operator + " " + operand2;
        this.resultProvider = Optional.ofNullable(RESULT_PROVIDER_BUILDERS.get(operator))
            .map(resultProviderBuilder -> resultProviderBuilder.apply(operand2))
            .orElseThrow(() -> new IllegalArgumentException("Wrong comparison operator: " + operator));
    }

    public Boolean getResult(Double operand1) {
        return resultProvider.apply(operand1);
    }
}
