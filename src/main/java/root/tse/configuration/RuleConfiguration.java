package root.tse.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import root.tse.application.rule.EntryRuleBuilder;
import root.tse.application.rule.ExitRuleBuilder;
import root.tse.application.rule.entry.ImmediatelySatisfiedOnlyOnce_EntryRuleBuilder;
import root.tse.application.rule.entry.PriceIs_EntryRuleBuilder;
import root.tse.application.rule.exit.PriceIs_ExitRuleBuilder;
import root.tse.application.rule.exit.ProfitOrLossValueIs_ExitRuleBuilder;
import root.tse.domain.ExchangeGateway;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class RuleConfiguration {

    @Bean
    public Map<String, EntryRuleBuilder> entryRuleBuilders(List<EntryRuleBuilder> ruleBuilders) {
        return ruleBuilders.stream().collect(Collectors.toMap(
            ruleBuilder -> ruleBuilder.getRuleDescription().getId(),
            Function.identity()));
    }
    @Bean
    public Map<String, ExitRuleBuilder> exitRuleBuilders(List<ExitRuleBuilder> ruleBuilders) {
        return ruleBuilders.stream().collect(Collectors.toMap(
            ruleBuilder -> ruleBuilder.getRuleDescription().getId(),
            Function.identity()));
    }

    // Entry rule builders

    @Bean
    public EntryRuleBuilder priceIs_EntryRuleBuilder(ExchangeGateway exchangeGateway) {
        return new PriceIs_EntryRuleBuilder(exchangeGateway);
    }
    @Bean
    public EntryRuleBuilder immediatelySatisfiedOnlyOnce_EntryRuleBuilder() {
        return new ImmediatelySatisfiedOnlyOnce_EntryRuleBuilder();
    }

    // Exit rule builders

    @Bean
    public ExitRuleBuilder priceIs_ExitRuleBuilder(ExchangeGateway exchangeGateway) {
        return new PriceIs_ExitRuleBuilder(exchangeGateway);
    }
    @Bean
    public ExitRuleBuilder profitOrLossValueIs_ExitRuleBuilder(ExchangeGateway exchangeGateway) {
        return new ProfitOrLossValueIs_ExitRuleBuilder(exchangeGateway);
    }
}
