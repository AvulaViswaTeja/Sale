package com.project.retailproject.dto;

import jakarta.validation.constraints.*;

public class SaleRequestDTO {
    @NotNull(message = "Product ID is required")
    private Long productId;
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    private String status;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
