package root.tse.infrastructure.persistence.trade;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.trade.Trade;
import root.tse.domain.strategy_execution.trade.TradeRepository;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class TradeRepositoryImpl implements TradeRepository {

    private final TradeToDbEntryMapper mapper;
    private final TradeDbEntryJpaRepository dbEntryRepository;

    @Override
    public void save(Trade trade) {
        var dbEntry = mapper.mapToDbEntry(trade);
        dbEntryRepository.saveAndFlush(dbEntry);
    }

    @Override
    public Collection<Trade> getAllTradesByStrategyExecutionId(String strategyExecutionId) {
        return dbEntryRepository.findAllByStrategyExecutionId(strategyExecutionId).stream()
            .map(mapper::mapToDomainObject)
            .collect(toList());
    }
}
