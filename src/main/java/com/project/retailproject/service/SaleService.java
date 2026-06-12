package com.project.retailproject.service;

import com.project.retailproject.clients.*;
import com.project.retailproject.db.SaleRepository;
import com.project.retailproject.dto.*;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.exception.ResourceNotFoundException;
import com.project.retailproject.model.Sale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private ProductClient productClient;
    @Autowired
    private CatalogClient catalogClient;
    @Autowired
    private InvoiceClient invoiceClient;
    @Autowired
    private AuditLogClient auditLogClient;

    private void log(String action,Long userId,String userName) {
        try {
            AuditLogRequestDTO dto = new AuditLogRequestDTO();
            dto.setAction(action);
            dto.setUserId(userId);
            dto.setUserName(userName);
            auditLogClient.createAuditLog(dto);
        }
        catch (Exception e) { System.err.println("AuditLog failed: " + e.getMessage()); }
    }

    @Transactional
    public SaleResponseDTO insertSale(SaleRequestDTO dto) {
        // 1. Get product
        ProductDTO product;
        try { product = productClient.getProductById(dto.getProductId()); }
        catch (Exception e) {
            throw new ResourceNotFoundException("Product not found with ID: " + dto.getProductId());
        }

        // 2. Product must be ACTIVE
        if (product.getStatus() == null || !product.getStatus().equalsIgnoreCase("ACTIVE")) {
            log("Sale.CREATE_FAILED | INACTIVE ProductID: " + dto.getProductId(),null,null);
            throw new BadRequestException("Cannot sell an inactive product");
        }

        // 3. Active catalog covering today
        boolean hasActiveCatalog;
        try {
            List<CatalogDTO> catalogs = catalogClient.getByProduct(dto.getProductId());
            LocalDate today = LocalDate.now();
            hasActiveCatalog = catalogs.stream().anyMatch(c ->
                    "ACTIVE".equalsIgnoreCase(c.getStatus())
                    && c.getEffectiveDate() != null && !c.getEffectiveDate().isAfter(today)
                    && c.getExpiryDate() != null && !c.getExpiryDate().isBefore(today));
        } catch (Exception e) {
            hasActiveCatalog = false;
        }
        if (!hasActiveCatalog) {
            log("Sale.CREATE_FAILED | No active catalog for ProductID: " + dto.getProductId(),null,null);
            throw new BadRequestException("Product has no active catalog listing for today");
        }

        // 4. Save sale
        double amount = product.getPrice() * dto.getQuantity();
        Sale sale = new Sale();
        sale.setProductId(dto.getProductId());
        sale.setProductName(product.getProductName());
        sale.setProductPrice(product.getPrice());
        sale.setCustomerId(dto.getCustomerId());
        sale.setQuantity(dto.getQuantity());
        sale.setAmount(amount);
        sale.setDate(LocalDate.now());
        sale.setStatus(dto.getStatus() != null ? dto.getStatus() : "COMPLETED");
        Sale saved = saleRepository.save(sale);
//      System.out.println(saved.getCustomerId());

        log("Sale.CREATE_SUCCESS | SaleID: " + saved.getSaleId()
                + " | ProductID: " + dto.getProductId()
                + " | CustomerID: " + dto.getCustomerId()
                + " | Qty: " + dto.getQuantity()
                + " | Amount: " + amount
                + " | Status: " + saved.getStatus(),null,null);

        // 5. Auto-create invoice if COMPLETED
        Long invoiceId = null;
        if (saved.getStatus().equalsIgnoreCase("COMPLETED")) {
            try {
                InvoiceResponseDTO inv = invoiceClient.insertInvoice(
                        new InvoiceRequestDTO(saved.getSaleId(), saved.getAmount(),saved.getCustomerId()));
                invoiceId = inv.getInvoiceId();
                log("Invoice.AUTO_GENERATED | InvoiceID: " + invoiceId + " | SaleID: " + saved.getSaleId(),null,null);
            } catch (Exception e) {
                log("Invoice.AUTO_GENERATED_FAILED | SaleID: " + saved.getSaleId() + " | " + e.getMessage(),null,null);
            }
        }
        return mapToDTO(saved, invoiceId);
    }

    public SaleResponseDTO updateSale(Long id, SaleRequestDTO dto) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
        if (sale.getStatus().equalsIgnoreCase("CANCELLED")) {
            log("Sale.UPDATE_FAILED | CANCELLED SaleID: " + id,null,null);
            throw new BadRequestException("Cannot update a cancelled sale");
        }
        String before = "Qty: " + sale.getQuantity() + " | Status: " + sale.getStatus()
                + " | Amount: " + sale.getAmount();
        double newAmount = sale.getProductPrice() * dto.getQuantity();
        sale.setQuantity(dto.getQuantity());
        sale.setStatus(dto.getStatus());
        sale.setAmount(newAmount);
        Sale saved = saleRepository.save(sale);
        log("Sale.UPDATE_SUCCESS | SaleID: " + id + " | Before: " + before
                + " | After: Qty: " + dto.getQuantity() + " | Status: " + dto.getStatus()
                + " | Amount: " + newAmount,null,null);
        Long invoiceId = null;
        try { invoiceId = invoiceClient.getInvoiceBySaleId(id).getInvoiceId(); } catch (Exception e) {}
        return mapToDTO(saved, invoiceId);
    }

    public void deleteSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
        sale.setStatus("CANCELLED");
        saleRepository.save(sale);
        try { invoiceClient.cancelInvoiceBySaleId(id); log("Invoice.AUTO_CANCELLED | SaleID: " + id,null,null); }
        catch (Exception e) {}
        log("Sale.CANCEL_SUCCESS | SaleID: " + id + " | CustomerID: " + sale.getCustomerId()
                + " | Amount: " + sale.getAmount() + " | Status: CANCELLED",null,null);
    }

    public SaleResponseDTO getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
        Long invoiceId = null;
        try { invoiceId = invoiceClient.getInvoiceBySaleId(id).getInvoiceId(); } catch (Exception e) {}
        return mapToDTO(sale, invoiceId);
    }

    public List<SaleResponseDTO> getAllSales() {
        return saleRepository.findAll().stream().map(this::withInvoice).collect(Collectors.toList());
    }
    public List<SaleResponseDTO> getSalesByCustomer(Long customerId) {
        return saleRepository.findByCustomerId(customerId).stream().map(this::withInvoice).collect(Collectors.toList());
    }
    public List<SaleResponseDTO> getSalesByDateRange(LocalDate start, LocalDate end) {
        return saleRepository.findByDateBetween(start, end).stream().map(this::withInvoice).collect(Collectors.toList());
    }
    public Page<SaleResponseDTO> getAllSalesPaginated(Pageable pageable) {
        return saleRepository.findAll(pageable).map(this::withInvoice);
    }

    private SaleResponseDTO withInvoice(Sale s) {
        Long invoiceId = null;
        try { invoiceId = invoiceClient.getInvoiceBySaleId(s.getSaleId()).getInvoiceId(); } catch (Exception e) {}
        return mapToDTO(s, invoiceId);
    }

    private SaleResponseDTO mapToDTO(Sale s, Long invoiceId) {
        SaleResponseDTO dto = new SaleResponseDTO();
        dto.setSaleId(s.getSaleId());
        dto.setProductId(s.getProductId());
        dto.setProductName(s.getProductName());
        dto.setCustomerId(s.getCustomerId());
        dto.setQuantity(s.getQuantity());
        dto.setAmount(s.getAmount());
        dto.setDate(s.getDate());
        dto.setStatus(s.getStatus());
        dto.setInvoiceId(invoiceId);
        return dto;
    }
}
