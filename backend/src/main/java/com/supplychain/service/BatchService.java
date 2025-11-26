package com.supplychain.service;

import com.supplychain.dto.*;
import com.supplychain.entity.*;
import com.supplychain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchService {

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SupplyChainEventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final BlockchainService blockchainService;

    @Transactional
    public BatchDTO createBatch(BatchDTO batchDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(batchDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (batchRepository.existsByBatchNumber(batchDTO.getBatchNumber())) {
            throw new RuntimeException("Batch number already exists");
        }

        Batch batch = Batch.builder()
                .productId(product.getId())
                .batchNumber(batchDTO.getBatchNumber())
                .quantity(batchDTO.getQuantity())
                .currentOwner(currentUser.getId())
                .currentLocation(batchDTO.getCurrentLocation())
                .status(Batch.BatchStatus.CREATED)
                .build();

        batch = batchRepository.save(batch);

        // Create initial supply chain event
        SupplyChainEvent event = createSupplyChainEvent(
                batch, currentUser.getId(), currentUser.getId(), 
                batchDTO.getCurrentLocation(), "CREATED"
        );

        return convertToDTO(batch, product, currentUser);
    }

    @Transactional
    public BatchDTO updateBatchStatus(Long batchId, BatchStatusUpdateDTO updateDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        User newOwner = userRepository.findById(updateDTO.getNewOwner())
                .orElseThrow(() -> new RuntimeException("New owner not found"));

        Long previousOwner = batch.getCurrentOwner();
        batch.setCurrentOwner(updateDTO.getNewOwner());
        batch.setCurrentLocation(updateDTO.getLocation());
        batch.setStatus(updateDTO.getStatus());

        batch = batchRepository.save(batch);

        // Create supply chain event
        SupplyChainEvent event = createSupplyChainEvent(
                batch, previousOwner, updateDTO.getNewOwner(),
                updateDTO.getLocation(), updateDTO.getStatus().name()
        );

        // If status is DELIVERED, trigger payment release
        if (updateDTO.getStatus() == Batch.BatchStatus.DELIVERED) {
            releasePayment(batch);
        }

        Product product = productRepository.findById(batch.getProductId()).orElseThrow();
        return convertToDTO(batch, product, newOwner);
    }

    public BatchDTO getBatch(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found"));
        
        Product product = productRepository.findById(batch.getProductId()).orElseThrow();
        User owner = userRepository.findById(batch.getCurrentOwner()).orElseThrow();
        
        return convertToDTO(batch, product, owner);
    }

    public List<BatchDTO> getAllBatches() {
        return batchRepository.findAll().stream()
                .map(batch -> {
                    Product product = productRepository.findById(batch.getProductId()).orElseThrow();
                    User owner = userRepository.findById(batch.getCurrentOwner()).orElseThrow();
                    return convertToDTO(batch, product, owner);
                })
                .collect(Collectors.toList());
    }

    public List<SupplyChainEventDTO> getBatchHistory(Long batchId) {
        List<SupplyChainEvent> events = eventRepository.findByBatchIdOrderByTimestampAsc(batchId);
        return events.stream().map(this::convertEventToDTO).collect(Collectors.toList());
    }

    public List<BatchDTO> getCurrentUserBatches() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return batchRepository.findByCurrentOwner(user.getId()).stream()
                .map(batch -> {
                    Product product = productRepository.findById(batch.getProductId()).orElseThrow();
                    return convertToDTO(batch, product, user);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    protected SupplyChainEvent createSupplyChainEvent(Batch batch, Long fromParty, Long toParty, 
                                                      String location, String status) {
        // Record event on blockchain
        SupplyChainEvent event = SupplyChainEvent.builder()
                .batchId(batch.getId())
                .productId(batch.getProductId())
                .fromParty(fromParty)
                .toParty(toParty)
                .location(location)
                .status(status)
                .timestamp(LocalDateTime.now())
                .verified(true)
                .build();

        String txHash = blockchainService.recordSupplyChainEvent(event);
        Long blockNumber = blockchainService.generateBlockNumber();

        event.setTxHash(txHash);
        event.setBlockNumber(blockNumber);

        return eventRepository.save(event);
    }

    @Transactional
    protected void releasePayment(Batch batch) {
        log.info("Releasing payment for batch: {}", batch.getId());
        
        String txHash = blockchainService.releasePaymentForBatch(batch.getId());
        
        Payment payment = Payment.builder()
                .batchId(batch.getId())
                .amount(BigDecimal.valueOf(1000.00)) // Simulate payment amount
                .status(Payment.PaymentStatus.RELEASED)
                .txHash(txHash)
                .releasedAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
        log.info("Payment released with tx hash: {}", txHash);
    }

    private BatchDTO convertToDTO(Batch batch, Product product, User owner) {
        return BatchDTO.builder()
                .id(batch.getId())
                .productId(batch.getProductId())
                .productName(product.getName())
                .batchNumber(batch.getBatchNumber())
                .quantity(batch.getQuantity())
                .currentOwner(batch.getCurrentOwner())
                .currentOwnerEmail(owner.getEmail())
                .currentLocation(batch.getCurrentLocation())
                .status(batch.getStatus())
                .createdAt(batch.getCreatedAt())
                .build();
    }

    private SupplyChainEventDTO convertEventToDTO(SupplyChainEvent event) {
        Product product = productRepository.findById(event.getProductId()).orElse(null);
        User fromUser = userRepository.findById(event.getFromParty()).orElse(null);
        User toUser = userRepository.findById(event.getToParty()).orElse(null);

        return SupplyChainEventDTO.builder()
                .id(event.getId())
                .batchId(event.getBatchId())
                .productId(event.getProductId())
                .productName(product != null ? product.getName() : "Unknown")
                .fromParty(event.getFromParty())
                .fromPartyEmail(fromUser != null ? fromUser.getEmail() : "Unknown")
                .toParty(event.getToParty())
                .toPartyEmail(toUser != null ? toUser.getEmail() : "Unknown")
                .location(event.getLocation())
                .status(event.getStatus())
                .timestamp(event.getTimestamp())
                .txHash(event.getTxHash())
                .blockNumber(event.getBlockNumber())
                .verified(event.getVerified())
                .build();
    }
}
