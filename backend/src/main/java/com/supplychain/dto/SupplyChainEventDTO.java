package com.supplychain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyChainEventDTO {
    private Long id;
    private Long batchId;
    private Long productId;
    private String productName;
    private Long fromParty;
    private String fromPartyEmail;
    private Long toParty;
    private String toPartyEmail;
    private String location;
    private String status;
    private LocalDateTime timestamp;
    private String txHash;
    private Long blockNumber;
    private Boolean verified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getFromParty() {
        return fromParty;
    }

    public void setFromParty(Long fromParty) {
        this.fromParty = fromParty;
    }

    public String getFromPartyEmail() {
        return fromPartyEmail;
    }

    public void setFromPartyEmail(String fromPartyEmail) {
        this.fromPartyEmail = fromPartyEmail;
    }

    public Long getToParty() {
        return toParty;
    }

    public void setToParty(Long toParty) {
        this.toParty = toParty;
    }

    public String getToPartyEmail() {
        return toPartyEmail;
    }

    public void setToPartyEmail(String toPartyEmail) {
        this.toPartyEmail = toPartyEmail;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}
