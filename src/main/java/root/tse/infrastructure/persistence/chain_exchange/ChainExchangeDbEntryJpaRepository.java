package root.tse.infrastructure.persistence.chain_exchange;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ChainExchangeDbEntryJpaRepository extends JpaRepository<ChainExchangeDbEntry, String> {

    Collection<ChainExchangeDbEntry> findAllByAssetChain(String assetChain);
}
