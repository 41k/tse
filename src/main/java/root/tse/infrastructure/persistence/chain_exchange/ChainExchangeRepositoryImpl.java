package root.tse.infrastructure.persistence.chain_exchange;

import lombok.RequiredArgsConstructor;
import root.tse.domain.chain_exchange_execution.ChainExchange;
import root.tse.domain.chain_exchange_execution.ChainExchangeRepository;

@RequiredArgsConstructor
public class ChainExchangeRepositoryImpl implements ChainExchangeRepository {

    private final ChainExchangeToDbEntryMapper mapper;
    private final ChainExchangeDbEntryJpaRepository dbEntryJpaRepository;

    @Override
    public void save(ChainExchange chainExchange) {
        var dbEntry = mapper.mapToDbEntry(chainExchange);
        dbEntryJpaRepository.saveAndFlush(dbEntry);
    }
}
