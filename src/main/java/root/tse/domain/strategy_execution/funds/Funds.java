package root.tse.domain.strategy_execution.funds;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Funds {

    private Double availableFunds;
    private Double fundsPerTrade;

    public boolean areNotEnough() {
        return availableFunds - fundsPerTrade < 0;
    }

    public void decrease() {
        availableFunds -= fundsPerTrade;
    }

    public void increase() {
        availableFunds += fundsPerTrade;
    }

    public Double calculateTradeAmount(Double price) {
        return fundsPerTrade / price;
    }
}
