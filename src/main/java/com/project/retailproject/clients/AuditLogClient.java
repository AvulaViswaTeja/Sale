package com.project.retailproject.clients;

import com.project.retailproject.config.FeignClientInterceptor;
import com.project.retailproject.dto.AuditLogRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "AuditLogClient", url = "${auditlog.service.url}",configuration = FeignClientInterceptor.class)
public interface AuditLogClient {
    @PostMapping("/api/audit-logs")
    void createAuditLog(@RequestBody AuditLogRequestDTO dto);
}
