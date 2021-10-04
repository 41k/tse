package root.tse.infrastructure.persistence.trade;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeDbEntryJpaRepository extends JpaRepository<TradeDbEntry, String> {
}
