package com.project.retailproject.clients;

import com.project.retailproject.config.FeignClientInterceptor;
import com.project.retailproject.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ProductClient", url = "${product.service.url}",configuration = FeignClientInterceptor.class)
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    ProductDTO getProduct(@PathVariable Long id);
}
