package com.supplychain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychain.dto.BatchDTO;
import com.supplychain.entity.Batch;
import com.supplychain.entity.Product;
import com.supplychain.entity.User;
import com.supplychain.repository.BatchRepository;
import com.supplychain.repository.ProductRepository;
import com.supplychain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchExportService {

    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public String exportBatchesAsCsv(List<BatchDTO> batches) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Batch Number,Product Name,Quantity,Current Location,Status,Created At\n");
        
        for (BatchDTO batch : batches) {
            csv.append(String.format("%d,%s,%s,%d,%s,%s,%s\n",
                    batch.getId(),
                    escapeCsv(batch.getBatchNumber()),
                    escapeCsv(batch.getProductName()),
                    batch.getQuantity(),
                    escapeCsv(batch.getCurrentLocation()),
                    batch.getStatus(),
                    batch.getCreatedAt()));
        }
        
        return csv.toString();
    }

    public String exportBatchesAsJson(List<BatchDTO> batches) throws Exception {
        return objectMapper.writeValueAsString(batches);
    }

    @Transactional
    public List<BatchDTO> importBatchesFromCsv(MultipartFile file) throws Exception {
        User currentUser = getCurrentUser();
        List<BatchDTO> importedBatches = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                String[] fields = line.split(",");
                if (fields.length < 5) continue;
                
                String batchNumber = fields[0].trim();
                String productName = fields[1].trim();
                int quantity = Integer.parseInt(fields[2].trim());
                String location = fields[3].trim();
                String status = fields[4].trim();
                
                // Find or create product
                Product product = productRepository.findAll().stream()
                        .filter(p -> p.getName().equalsIgnoreCase(productName))
                        .findFirst()
                        .orElseGet(() -> {
                            Product newProduct = Product.builder()
                                    .name(productName)
                                    .description("Imported product")
                                    .category("General")
                                    .sku("SKU-" + System.currentTimeMillis())
                                    .createdBy(currentUser.getId())
                                    .build();
                            return productRepository.save(newProduct);
                        });
                
                // Create batch
                Batch batch = Batch.builder()
                        .productId(product.getId())
                        .batchNumber(batchNumber)
                        .quantity(quantity)
                        .currentOwner(currentUser.getId())
                        .currentLocation(location)
                        .status(Batch.BatchStatus.valueOf(status))
                        .build();
                
                batch = batchRepository.save(batch);
                
                User owner = userRepository.findById(batch.getCurrentOwner()).orElse(null);
                
                importedBatches.add(BatchDTO.builder()
                        .id(batch.getId())
                        .productId(batch.getProductId())
                        .productName(product.getName())
                        .batchNumber(batch.getBatchNumber())
                        .quantity(batch.getQuantity())
                        .currentOwner(batch.getCurrentOwner())
                        .currentOwnerEmail(owner != null ? owner.getEmail() : "Unknown")
                        .currentLocation(batch.getCurrentLocation())
                        .status(Batch.BatchStatus.valueOf(batch.getStatus().name()))
                        .createdAt(LocalDateTime.parse(batch.getCreatedAt().toString()))
                        .build());
            }
        }
        
        return importedBatches;
    }

    @Transactional
    public List<BatchDTO> importBatchesFromJson(MultipartFile file) throws Exception {
        User currentUser = getCurrentUser();
        List<BatchDTO> importedBatches = new ArrayList<>();

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        List<?> batchesData = objectMapper.readValue(content, List.class);
        
        for (Object obj : batchesData) {
            @SuppressWarnings("unchecked")
            var batchMap = (java.util.Map<String, Object>) obj;
            
            String batchNumber = (String) batchMap.get("batchNumber");
            String productName = (String) batchMap.get("productName");
            int quantity = (Integer) batchMap.get("quantity");
            String location = (String) batchMap.get("currentLocation");
            String status = (String) batchMap.get("status");
            
            // Find or create product
            Product product = productRepository.findAll().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(productName))
                    .findFirst()
                    .orElseGet(() -> {
                        Product newProduct = Product.builder()
                                .name(productName)
                                .description("Imported product")
                                .category("General")
                                .sku("SKU-" + System.currentTimeMillis())
                                .createdBy(currentUser.getId())
                                .build();
                        return productRepository.save(newProduct);
                    });
            
            // Create batch
            Batch batch = Batch.builder()
                    .productId(product.getId())
                    .batchNumber(batchNumber)
                    .quantity(quantity)
                    .currentOwner(currentUser.getId())
                    .currentLocation(location)
                    .status(Batch.BatchStatus.valueOf(status))
                    .build();
            
            batch = batchRepository.save(batch);
            
            User owner = userRepository.findById(batch.getCurrentOwner()).orElse(null);
            
            importedBatches.add(BatchDTO.builder()
                    .id(batch.getId())
                    .productId(batch.getProductId())
                    .productName(product.getName())
                    .batchNumber(batch.getBatchNumber())
                    .quantity(batch.getQuantity())
                    .currentOwner(batch.getCurrentOwner())
                    .currentOwnerEmail(owner != null ? owner.getEmail() : "Unknown")
                    .currentLocation(batch.getCurrentLocation())
                    .status(Batch.BatchStatus.valueOf(batch.getStatus().name()))
                    .createdAt(LocalDateTime.parse(batch.getCreatedAt().toString()))
                    .build());
        }
        
        return importedBatches;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
