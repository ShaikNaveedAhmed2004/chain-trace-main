package com.supplychain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "supply_chain_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyChainEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long batchId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long fromParty;

    @Column(nullable = false)
    private Long toParty;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String txHash;

    @Column(nullable = false)
    private Long blockNumber;

    @Column(nullable = false)
    private Boolean verified;

     @Column(name = "product_name", nullable = false)
    private String productName;
}
