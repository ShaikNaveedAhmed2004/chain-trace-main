package com.supplychain.repository;

import com.supplychain.entity.SupplyChainEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplyChainEventRepository extends JpaRepository<SupplyChainEvent, Long> {
    List<SupplyChainEvent> findByBatchIdOrderByTimestampAsc(Long batchId);
    List<SupplyChainEvent> findByProductIdOrderByTimestampDesc(Long productId);

}
