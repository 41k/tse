export class OrderExecution {
  constructor(
    public id: string,
    public orderType: string,
    public orderExecutionType: string,
    public symbol: string,
    public amount: number,
    public price: number,
    public timestamp: number,
    public ruleDescription: Array<string>
  ) {}
}
