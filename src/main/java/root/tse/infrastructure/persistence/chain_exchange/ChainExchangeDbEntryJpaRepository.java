package root.tse.infrastructure.persistence.chain_exchange;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChainExchangeDbEntryJpaRepository extends JpaRepository<ChainExchangeDbEntry, String> {
}
