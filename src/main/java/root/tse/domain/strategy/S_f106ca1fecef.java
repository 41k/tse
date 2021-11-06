package root.tse.domain.strategy;

import root.tse.domain.strategy_execution.ExchangeGateway;
import root.tse.domain.strategy_execution.Interval;

public class S_f106ca1fecef extends AbstractSimpleBreakoutStrategy {

    public S_f106ca1fecef(ExchangeGateway exchangeGateway) {
        super(30, Interval.ONE_DAY, 7, 10, exchangeGateway);
    }

    @Override
    public String getId() { return "f106ca1fecef"; }
}
