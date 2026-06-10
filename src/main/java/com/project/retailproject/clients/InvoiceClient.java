package com.project.retailproject.clients;

import com.project.retailproject.dto.InvoiceRequestDTO;
import com.project.retailproject.dto.InvoiceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "InvoiceClient", url = "${invoice.service.url}")
public interface InvoiceClient {
    @PostMapping("/api/invoices")
    InvoiceResponseDTO insertInvoice(@RequestBody InvoiceRequestDTO dto);

    @GetMapping("/api/invoices/sale/{saleId}")
    InvoiceResponseDTO getInvoiceBySaleId(@PathVariable Long saleId);

    @DeleteMapping("/api/invoices/sale/{saleId}")
    void cancelInvoiceBySaleId(@PathVariable Long saleId);
}
