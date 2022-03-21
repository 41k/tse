export class StrategyExecution {
  constructor(
    public id: string,
    public orderExecutionType: string,
    public symbols: Array<string>,
    public fundsPerTrade: number,
    public entryRuleDescription: Array<string>,
    public exitRuleDescription: Array<string>
  ) {}
}
