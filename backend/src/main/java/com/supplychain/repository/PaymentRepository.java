package com.supplychain.repository;

import com.supplychain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBatchId(Long batchId);
    List<Payment> findByStatus(Payment.PaymentStatus status);
}
