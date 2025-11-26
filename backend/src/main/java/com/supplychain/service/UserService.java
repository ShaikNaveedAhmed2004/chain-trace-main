package com.supplychain.service;

import com.supplychain.dto.UpdateEmailRequest;
import com.supplychain.dto.UpdatePasswordRequest;
import com.supplychain.dto.UserActivityDTO;
import com.supplychain.dto.UserDTO;
import com.supplychain.entity.Batch;
import com.supplychain.entity.Product;
import com.supplychain.entity.SupplyChainEvent;
import com.supplychain.entity.User;
import com.supplychain.repository.BatchRepository;
import com.supplychain.repository.ProductRepository;
import com.supplychain.repository.SupplyChainEventRepository;
import com.supplychain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final SupplyChainEventRepository supplyChainEventRepository;

    public UserDTO getCurrentUserProfile() {
        User user = getCurrentUser();
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateEmail(UpdateEmailRequest request) {
        User user = getCurrentUser();
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Check if new email already exists
        if (userRepository.existsByEmail(request.getNewEmail()) && !user.getEmail().equals(request.getNewEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        user.setEmail(request.getNewEmail());
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        User user = getCurrentUser();
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public List<UserActivityDTO> getUserActivity() {
        User user = getCurrentUser();
        List<UserActivityDTO> activities = new ArrayList<>();

        // Get batches created by user
        List<Batch> batches = batchRepository.findByCurrentOwner(user.getId());
        for (Batch batch : batches) {
            Product product = productRepository.findById(batch.getProductId()).orElse(null);
            activities.add(UserActivityDTO.builder()
                    .id(batch.getId())
                    .activityType("BATCH_CREATED")
                    .description("Created batch " + batch.getBatchNumber())
                    .batchId(batch.getId())
                    .batchNumber(batch.getBatchNumber())
                    .productId(batch.getProductId())
                    .productName(product != null ? product.getName() : "Unknown")
                    .timestamp(batch.getCreatedAt())
                    .build());
        }

        // Get products created by user
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> p.getCreatedBy().equals(user.getId()))
                .collect(Collectors.toList());
        for (Product product : products) {
            activities.add(UserActivityDTO.builder()
                    .id(product.getId())
                    .activityType("PRODUCT_CREATED")
                    .description("Created product " + product.getName())
                    .productId(product.getId())
                    .productName(product.getName())
                    .timestamp(product.getCreatedAt())
                    .build());
        }

        // Get supply chain events where user was involved
        List<SupplyChainEvent> events = supplyChainEventRepository.findAll().stream()
                .filter(e -> e.getFromParty().equals(user.getId()) || e.getToParty().equals(user.getId()))
                .collect(Collectors.toList());
        for (SupplyChainEvent event : events) {
            String description = event.getFromParty().equals(user.getId()) 
                ? "Transferred batch to " + event.getFromParty()
                : "Received batch from " + event.getFromParty();
            activities.add(UserActivityDTO.builder()
                    .id(event.getId())
                    .activityType("BATCH_UPDATED")
                    .description(description + " - " + event.getStatus())
                    .batchId(event.getBatchId())
                    .productId(event.getProductId())
                    .productName(event.getProductName())
                    .timestamp(event.getTimestamp())
                    .build());
        }

        // Sort by timestamp descending
        activities.sort(Comparator.comparing(UserActivityDTO::getTimestamp).reversed());
        
        return activities;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
