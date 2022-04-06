package root.tse.application.rule;

import lombok.RequiredArgsConstructor;
import root.tse.domain.rule.EntryRule;
import root.tse.domain.rule.ExitRule;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RuleService {

    private final Map<String, EntryRuleBuilder> entryRuleBuilders;
    private final Map<String, ExitRuleBuilder> exitRuleBuilders;

    public EntryRule buildEntryRule(RuleContext context) {
        return Optional.ofNullable(entryRuleBuilders.get(context.getRuleId()))
            .map(ruleBuilder -> ruleBuilder.build(context))
            .orElseThrow(() -> new IllegalArgumentException("Invalid entry rule id"));
    }

    public ExitRule buildExitRule(RuleContext context) {
        return Optional.ofNullable(exitRuleBuilders.get(context.getRuleId()))
            .map(ruleBuilder -> ruleBuilder.build(context))
            .orElseThrow(() -> new IllegalArgumentException("Invalid exit rule id"));
    }

    public Collection<RuleDescription> getEntryRulesDescriptions() {
        return entryRuleBuilders.values().stream().map(RuleBuilder::getRuleDescription).collect(Collectors.toList());
    }

    public Collection<RuleDescription> getExitRulesDescriptions() {
        return exitRuleBuilders.values().stream().map(RuleBuilder::getRuleDescription).collect(Collectors.toList());
    }
}
