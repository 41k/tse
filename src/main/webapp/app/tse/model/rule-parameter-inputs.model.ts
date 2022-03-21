import { RuleParameterInput } from 'app/tse/model/rule-parameter-input.model';

export const RuleParameterInputs: Record<string, RuleParameterInput> = {
  PROFIT_VALUE: new RuleParameterInput('Profit value', 'number'),
  LOSS_VALUE: new RuleParameterInput('Loss value', 'number'),
  TARGET_PRICE: new RuleParameterInput('Target price', 'number'),
  COMPARISON_OPERATOR: new RuleParameterInput('Comparison operator', 'text'),
} as const;
