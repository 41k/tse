package root.tse.infrastructure.persistence.trade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface TradeDbEntryJpaRepository extends JpaRepository<TradeDbEntry, String> {

    Collection<TradeDbEntry> findAllByStrategyExecutionId(String strategyExecutionId);
}
