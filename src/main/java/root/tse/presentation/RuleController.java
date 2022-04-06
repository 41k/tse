package root.tse.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.tse.application.rule.RuleDescription;
import root.tse.application.rule.RuleService;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping("/entry")
    public Collection<RuleDescription> getEntryRules() {
        return ruleService.getEntryRulesDescriptions();
    }

    @GetMapping("/exit")
    public Collection<RuleDescription> getExitRules() {
        return ruleService.getExitRulesDescriptions();
    }
}
