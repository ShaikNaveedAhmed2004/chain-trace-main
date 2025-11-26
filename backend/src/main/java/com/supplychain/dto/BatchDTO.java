package com.supplychain.dto;

import com.supplychain.entity.Batch;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDTO {
    private Long id;
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    private String productName;
    
    @NotBlank(message = "Batch number is required")
    private String batchNumber;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private Long currentOwner;
    private String currentOwnerEmail;
    
    @NotBlank(message = "Location is required")
    private String currentLocation;
    
    @NotNull(message = "Status is required")
    private Batch.BatchStatus status;
    
    private LocalDateTime createdAt;
}
