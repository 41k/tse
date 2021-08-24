package root.tse.domain.strategy_execution.funds;

public interface FundsRepository {

    Funds get();

    void save(Funds funds);
}
