package com.supplychain.repository;

import com.supplychain.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    Optional<Batch> findByBatchNumber(String batchNumber);
    List<Batch> findByCurrentOwner(Long currentOwner);
    List<Batch> findByProductId(Long productId);
    boolean existsByBatchNumber(String batchNumber);
}
