import { EquityCurvePoint } from 'app/tse/model/equity-curve-point.model';

export class Report {
  constructor(
    public strategyExecutionId: string,
    public symbols: Array<string>,
    public fundsPerTrade: number,
    public orderFeePercent: number,

    public equityCurve: Array<EquityCurvePoint>,
    public ntrades: number,
    public nclosedTrades: number,
    public nprofitableTrades: number,
    public totalProfit: number
  ) {}
}
