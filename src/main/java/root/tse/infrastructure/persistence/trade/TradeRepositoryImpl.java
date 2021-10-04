package root.tse.infrastructure.persistence.trade;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.trade.Trade;
import root.tse.domain.strategy_execution.trade.TradeRepository;

@RequiredArgsConstructor
public class TradeRepositoryImpl implements TradeRepository {

    private final TradeToDbEntryMapper mapper;
    private final TradeDbEntryJpaRepository dbEntryRepository;

    @Override
    public void save(Trade trade) {
        var dbEntry = mapper.mapToDbEntry(trade);
        dbEntryRepository.save(dbEntry);
    }
}
