package root.tse.domain.strategy_execution.funds;

import lombok.RequiredArgsConstructor;

import javax.transaction.Transactional;

@RequiredArgsConstructor
public class FundsManager {

    private final FundsRepository fundsRepository;

    @Transactional
    public Double acquireFundsAndProvideTradeAmount(Double price) {
        var funds = fundsRepository.get();
        if (funds.areNotEnough()) {
            return null;
        }
        funds.decrease();
        fundsRepository.save(funds);
        return funds.calculateTradeAmount(price);
    }

    @Transactional
    public void returnFunds() {
        var funds = fundsRepository.get();
        funds.increase();
        fundsRepository.save(funds);
    }
}
