package com.project.retailproject.dto;

public class InvoiceRequestDTO {
    private Long saleId;
    private Double amount;
    private Long customerId;

    public InvoiceRequestDTO() {}
    public InvoiceRequestDTO(Long saleId, Double amount,Long customerId) {
        this.saleId = saleId;
        this.amount = amount;
        this.customerId = customerId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getSaleId() { return saleId; }
    public void setSaleId(Long saleId) { this.saleId = saleId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
