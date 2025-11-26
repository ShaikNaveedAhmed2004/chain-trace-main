package com.supplychain.service;

import com.supplychain.dto.*;
import com.supplychain.entity.*;
import com.supplychain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SupplyChainEventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final BatchService batchService;

    public VerificationResponse verifyBatch(Long batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        Product product = productRepository.findById(batch.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        User owner = userRepository.findById(batch.getCurrentOwner())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        User creator = userRepository.findById(product.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        List<SupplyChainEvent> events = eventRepository.findByBatchIdOrderByTimestampAsc(batchId);
        List<SupplyChainEventDTO> eventDTOs = events.stream()
                .map(this::convertEventToDTO)
                .collect(Collectors.toList());

        boolean allVerified = events.stream().allMatch(SupplyChainEvent::getVerified);

        // Get payment information
        Payment payment = paymentRepository.findByBatchId(batchId).orElse(null);

        BatchDTO batchDTO = BatchDTO.builder()
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

        ProductDTO productDTO = ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .sku(product.getSku())
                .createdBy(product.getCreatedBy())
                .createdByEmail(creator.getEmail())
                .createdAt(product.getCreatedAt())
                .build();

        return VerificationResponse.builder()
                .batch(batchDTO)
                .product(productDTO)
                .events(eventDTOs)
                .allEventsVerified(allVerified)
                .paymentStatus(payment != null ? payment.getStatus() : Payment.PaymentStatus.PENDING)
                .paymentAmount(payment != null ? payment.getAmount() : null)
                .paymentTxHash(payment != null ? payment.getTxHash() : null)
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
