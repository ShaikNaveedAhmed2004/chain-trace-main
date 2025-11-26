package com.supplychain.controller;

import com.supplychain.dto.VerificationResponse;
import com.supplychain.service.ConsumerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerService consumerService;

    @GetMapping("/verify")
    public ResponseEntity<VerificationResponse> verifyBatch(@RequestParam Long batchId) {
        return ResponseEntity.ok(consumerService.verifyBatch(batchId));
    }
}
