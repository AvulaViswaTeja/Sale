package com.project.retailproject.clients;

import com.project.retailproject.dto.CatalogDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "CatalogClient", url = "${catalog.service.url}")
public interface CatalogClient {
    @GetMapping("/api/catalogs/product/{productId}")
    List<CatalogDTO> getByProduct(@PathVariable Long productId);
}
