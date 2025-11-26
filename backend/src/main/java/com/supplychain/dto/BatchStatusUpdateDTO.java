package com.supplychain.dto;

import com.supplychain.entity.Batch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchStatusUpdateDTO {
    @NotNull(message = "Status is required")
    private Batch.BatchStatus status;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotNull(message = "New owner is required")
    private Long newOwner;
}
