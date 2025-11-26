package com.supplychain.dto;

import com.supplychain.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {
    private BatchDTO batch;
    private ProductDTO product;
    private List<SupplyChainEventDTO> events;
    private Boolean allEventsVerified;
    private Payment.PaymentStatus paymentStatus;
    private BigDecimal paymentAmount;
    private String paymentTxHash;
}
