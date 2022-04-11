import { RuleParameterInput } from 'app/tse/model/rule-parameter-input.model';

export const RuleParameterInputs: Record<string, RuleParameterInput> = {
  'profit-value': new RuleParameterInput('Profit value', 'number'),
  'loss-value': new RuleParameterInput('Loss value', 'number'),
  'comparison-operator': new RuleParameterInput('Comparison operator', 'text'),
  'target-price': new RuleParameterInput('Target price', 'number'),
} as const;
