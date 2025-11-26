package com.supplychain.controller;

import com.supplychain.dto.BatchDTO;
import com.supplychain.dto.BatchStatusUpdateDTO;
import com.supplychain.dto.SupplyChainEventDTO;
import com.supplychain.service.BatchExportService;
import com.supplychain.service.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;
    private final BatchExportService batchExportService;

    @PostMapping
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<BatchDTO> createBatch(@Valid @RequestBody BatchDTO batchDTO) {
        return ResponseEntity.ok(batchService.createBatch(batchDTO));
    }

    @GetMapping
    public ResponseEntity<List<BatchDTO>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BatchDTO> getBatch(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.getBatch(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANUFACTURER', 'DISTRIBUTOR', 'RETAILER')")
    public ResponseEntity<BatchDTO> updateBatchStatus(
            @PathVariable Long id,
            @Valid @RequestBody BatchStatusUpdateDTO updateDTO) {
        return ResponseEntity.ok(batchService.updateBatchStatus(id, updateDTO));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<SupplyChainEventDTO>> getBatchHistory(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.getBatchHistory(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BatchDTO>> getMyBatches() {
        return ResponseEntity.ok(batchService.getCurrentUserBatches());
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportBatchesCsv() {
        List<BatchDTO> batches = batchService.getAllBatches();
        String csv = batchExportService.exportBatchesAsCsv(batches);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=batches.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/export/json")
    public ResponseEntity<String> exportBatchesJson() throws Exception {
        List<BatchDTO> batches = batchService.getAllBatches();
        String json = batchExportService.exportBatchesAsJson(batches);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=batches.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    @PostMapping("/import/csv")
    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    public ResponseEntity<List<BatchDTO>> importBatchesCsv(@RequestParam("file") MultipartFile file) throws Exception {
        List<BatchDTO> imported = batchExportService.importBatchesFromCsv(file);
        return ResponseEntity.ok(imported);
    }

    @PostMapping("/import/json")
    @PreAuthorize("hasAnyRole('SUPPLIER', 'ADMIN')")
    public ResponseEntity<List<BatchDTO>> importBatchesJson(@RequestParam("file") MultipartFile file) throws Exception {
        List<BatchDTO> imported = batchExportService.importBatchesFromJson(file);
        return ResponseEntity.ok(imported);
    }
}
