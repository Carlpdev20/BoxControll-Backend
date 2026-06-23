package com.proyecto.member.client;

import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "billing-service", url = "http://billing-service:8082")
public interface BillingFeignClient {

	@PostMapping("/api/billing/invoices")
    void crearCuota(
        @RequestHeader("X-Tenant-Id") UUID tenantId, // Pasa el tenant de forma transparente
        @RequestBody Map<String, Object> invoiceData  // Enviamos los datos como mapa para evitar duplicar DTOs entre proyectos
    );
}
