import { Component, ElementRef, ViewChild } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { SymbolApiClient } from 'app/tse/api-client/symbol.api-client';
import { RuleApiClient } from 'app/tse/api-client/rule.api-client';
import { OrderExecutionApiClient } from 'app/tse/api-client/order-execution.api-client';
import { RuleParameterInputs } from 'app/tse/model/rule-parameter-inputs.model';
import { Rule } from 'app/tse/model/rule.model';

@Component({
  selector: 'jhi-new-order-execution',
  templateUrl: './new-order-execution.component.html',
})
export class NewOrderExecutionComponent {
  @ViewChild('ruleInputs') ruleInputs!: ElementRef<HTMLElement>;

  symbols?: string[] | null;
  rules?: Rule[] | null;
  ruleParameterInputs = RuleParameterInputs;

  selectedOrderExecutionType?: string;
  selectedOrderType?: string;
  selectedSymbol?: string;
  amount?: number;
  selectedRule?: Rule;

  constructor(
    private symbolApiClient: SymbolApiClient,
    private ruleApiClient: RuleApiClient,
    private orderExecutionApiClient: OrderExecutionApiClient,
    private router: Router
  ) {
    this.initViewData();
  }

  viewDataIsInitialized(): boolean {
    return this.symbols != null && this.rules != null;
  }

  startNewOrderExecution(): void {
    if (!this.selectedOrderExecutionType || !this.selectedOrderType || !this.selectedSymbol || !this.amount || !this.selectedRule) {
      alert('Form is not completed');
      return;
    }
    const ruleParams = this.collectRuleParameters(this.selectedRule, this.ruleInputs);
    const confirmationMessage = this.formConfirmationMessage(ruleParams);
    if (confirm(confirmationMessage)) {
      this.orderExecutionApiClient
        .startOrderExecution({
          orderExecutionType: this.selectedOrderExecutionType,
          orderType: this.selectedOrderType,
          symbol: this.selectedSymbol,
          amount: this.amount,
          ruleId: this.selectedRule.id,
          ruleParameters: ruleParams,
        })
        .subscribe(
          () => {
            this.router.navigate(['order-executions']);
          },
          () => alert('FAILED to start new order execution')
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

  private formConfirmationMessage(ruleParameters: object): string {
    let message = '\n[SETTINGS]';
    message += '\nOrder execution type: ' + this.selectedOrderExecutionType;
    message += '\nOrder type: ' + this.selectedOrderType;
    message += '\nSymbol: ' + this.selectedSymbol;
    message += '\nAmount: ' + this.amount;
    message += '\n\n[RULE]';
    message += '\nName: ' + this.selectedRule?.name;
    Object.entries(ruleParameters).forEach(([key, value]) => (message += '\n' + this.ruleParameterInputs[key].label + ': ' + value));
    return message;
  }

  private initViewData(): void {
    this.symbolApiClient.getSymbols().subscribe((response: HttpResponse<string[]>) => {
      this.symbols = response.body;
    });
    this.ruleApiClient.getEntryRules().subscribe((response: HttpResponse<Rule[]>) => {
      this.rules = response.body;
    });
  }
}
