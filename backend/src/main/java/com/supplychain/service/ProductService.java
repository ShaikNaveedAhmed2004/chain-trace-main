package com.supplychain.service;

import com.supplychain.dto.ProductDTO;
import com.supplychain.entity.Product;
import com.supplychain.entity.User;
import com.supplychain.repository.ProductRepository;
import com.supplychain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new RuntimeException("Product SKU already exists");
        }

        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .category(productDTO.getCategory())
                .sku(productDTO.getSku())
                .createdBy(currentUser.getId())
                .build();

        product = productRepository.save(product);
        return convertToDTO(product, currentUser);
    }

    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        User creator = userRepository.findById(product.getCreatedBy()).orElseThrow();
        return convertToDTO(product, creator);
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> {
                    User creator = userRepository.findById(product.getCreatedBy()).orElseThrow();
                    return convertToDTO(product, creator);
                })
                .collect(Collectors.toList());
    }

    private ProductDTO convertToDTO(Product product, User creator) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .sku(product.getSku())
                .createdBy(product.getCreatedBy())
                .createdByEmail(creator.getEmail())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
