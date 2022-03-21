import { Component, ElementRef, ViewChild } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { SymbolApiClient } from 'app/tse/api-client/symbol.api-client';
import { RuleApiClient } from 'app/tse/api-client/rule.api-client';
import { StrategyExecutionApiClient } from 'app/tse/api-client/strategy-execution.api-client';
import { RuleParameterInputs } from 'app/tse/model/rule-parameter-inputs.model';
import { Rule } from 'app/tse/model/rule.model';

@Component({
  selector: 'jhi-new-strategy-execution',
  templateUrl: './new-strategy-execution.component.html',
})
export class NewStrategyExecutionComponent {
  @ViewChild('entryRuleInputs') entryRuleInputs!: ElementRef<HTMLElement>;
  @ViewChild('exitRuleInputs') exitRuleInputs!: ElementRef<HTMLElement>;

  symbols?: string[] | null;
  entryRules?: Rule[] | null;
  exitRules?: Rule[] | null;
  ruleParameterInputs = RuleParameterInputs;

  selectedOrderExecutionType?: string;
  selectedSymbol?: string;
  fundsPerTrade?: number;
  selectedEntryRule?: Rule;
  selectedExitRule?: Rule;

  constructor(
    private symbolApiClient: SymbolApiClient,
    private ruleApiClient: RuleApiClient,
    private strategyExecutionApiClient: StrategyExecutionApiClient,
    private router: Router
  ) {
    this.initViewData();
  }

  viewDataIsInitialized(): boolean {
    return this.symbols != null && this.entryRules != null && this.exitRules != null;
  }

  startNewStrategyExecution(): void {
    if (
      !this.selectedOrderExecutionType ||
      !this.selectedSymbol ||
      !this.fundsPerTrade ||
      !this.selectedEntryRule ||
      !this.selectedExitRule
    ) {
      alert('Form is not completed');
      return;
    }
    const entryRuleParams = this.collectRuleParameters(this.selectedEntryRule, this.entryRuleInputs);
    const exitRuleParams = this.collectRuleParameters(this.selectedExitRule, this.exitRuleInputs);
    const confirmationMessage = this.formConfirmationMessage(entryRuleParams, exitRuleParams);
    if (confirm(confirmationMessage)) {
      this.strategyExecutionApiClient
        .startStrategyExecution({
          orderExecutionType: this.selectedOrderExecutionType,
          symbol: this.selectedSymbol,
          fundsPerTrade: this.fundsPerTrade,
          entryRuleId: this.selectedEntryRule.id,
          exitRuleId: this.selectedExitRule.id,
          entryRuleParameters: entryRuleParams,
          exitRuleParameters: exitRuleParams,
        })
        .subscribe(
          () => {
            this.router.navigate(['strategy-executions']);
          },
          () => alert('FAILED to start new strategy execution')
        );
    }
  }

  private collectRuleParameters(rule: Rule, inputsWrapper: ElementRef<HTMLElement>): object {
    const ruleParameters = {};
    rule.parameters?.forEach(parameter => {
      ruleParameters[parameter] = (inputsWrapper.nativeElement.querySelector(`input[name="${parameter}"]`) as HTMLInputElement).value;
    });
    return ruleParameters;
  }

  private formConfirmationMessage(entryRuleParameters: object, exitRuleParameters: object): string {
    let message = '\n[SETTINGS]';
    message += '\nOrder execution type: ' + this.selectedOrderExecutionType;
    message += '\nSymbol: ' + this.selectedSymbol;
    message += '\nFunds per trade: ' + this.fundsPerTrade;
    message += '\n\n[ENTRY RULE]';
    message += '\nName: ' + this.selectedEntryRule?.name;
    Object.entries(entryRuleParameters).forEach(([key, value]) => (message += '\n' + this.ruleParameterInputs[key].label + ': ' + value));
    message += '\n\n[EXIT RULE]';
    message += '\nName: ' + this.selectedExitRule?.name;
    Object.entries(exitRuleParameters).forEach(([key, value]) => (message += '\n' + this.ruleParameterInputs[key].label + ': ' + value));
    return message;
  }

  private initViewData(): void {
    this.symbolApiClient.getSymbols().subscribe((response: HttpResponse<string[]>) => {
      this.symbols = response.body;
    });
    this.ruleApiClient.getEntryRules().subscribe((response: HttpResponse<Rule[]>) => {
      this.entryRules = response.body;
    });
    this.ruleApiClient.getExitRules().subscribe((response: HttpResponse<Rule[]>) => {
      this.exitRules = response.body;
    });
  }
}
