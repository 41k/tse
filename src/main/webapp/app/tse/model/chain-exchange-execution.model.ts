export class ChainExchangeExecution {
  constructor(
    public assetChainId: number,
    public assetChain: Array<string>,
    public orderExecutionType: string,
    public amount: number,
    public minProfitThreshold: number
  ) {}
}
