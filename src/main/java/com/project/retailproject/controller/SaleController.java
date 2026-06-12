package com.project.retailproject.controller;

import com.project.retailproject.dto.SaleRequestDTO;
import com.project.retailproject.dto.SaleResponseDTO;
import com.project.retailproject.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    @Autowired private SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponseDTO> createSale(@RequestBody SaleRequestDTO dto) {
        return ResponseEntity.ok(saleService.insertSale(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponseDTO> updateSale(@PathVariable Long id, @RequestBody SaleRequestDTO dto) {
        return ResponseEntity.ok(saleService.updateSale(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponseDTO> getSale(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSaleById(id));
    }

    @GetMapping
    public ResponseEntity<List<SaleResponseDTO>> getAllSales() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SaleResponseDTO>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(saleService.getSalesByCustomer(customerId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<SaleResponseDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(saleService.getSalesByDateRange(start, end));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<SaleResponseDTO>> getPaginated(Pageable pageable) {
        return ResponseEntity.ok(saleService.getAllSalesPaginated(pageable));
    }
}
