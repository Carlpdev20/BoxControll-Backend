package com.proyecto.billing.controller;

import com.proyecto.billing.dto.InvoiceRequestDTO;
import com.proyecto.billing.dto.InvoiceResponseDTO;
import com.proyecto.billing.dto.PaymentRequestDTO;
import com.proyecto.billing.dto.PaymentResponseDTO;
import com.proyecto.billing.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
public class BillingController {

    @Autowired
    private BillingService billingService;

    // ============================================================================
    // 1. ENDPOINT: REGISTRAR UN PAGO (EFECTIVO O TRANSFERENCIA)
    // ============================================================================
    @PostMapping("/payments")
    public ResponseEntity<?> registrarPago(
            @RequestHeader("X-Tenant-Id") String tenantIdHeader, // 🔒 Inyectado automáticamente por el Gateway
            @RequestBody PaymentRequestDTO paymentRequest) {
        try {
            UUID tenantId = UUID.fromString(tenantIdHeader);
            PaymentResponseDTO response = billingService.registrarPago(tenantId, paymentRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("El Tenant ID provisto no tiene un formato UUID válido.");
        } catch (RuntimeException e) {
            // Captura errores de negocio (ej: falta nro de operación o cuota no encontrada)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno en el módulo de facturación");
        }
    }
    
    @PostMapping("/invoices")
    public ResponseEntity<?> crearCuota(
            @RequestHeader("X-Tenant-Id") String tenantIdHeader,
            @RequestBody InvoiceRequestDTO dto) {
        try {
            UUID tenantId = UUID.fromString(tenantIdHeader);
            InvoiceResponseDTO response = billingService.crearCuotaInmediata(tenantId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al automatizar la cuota: " + e.getMessage());
        }
    }

    // ============================================================================
    // 3. ENDPOINT: LISTAR CUOTAS MOROSAS (SOPORTE OPERATIVO PARA CARLOS)
    // ============================================================================
    @GetMapping("/invoices/overdue")
    public ResponseEntity<?> listarMorosos(
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) { // 🔒 Inyectado automáticamente por el Gateway
        try {
            UUID tenantId = UUID.fromString(tenantIdHeader);
            List<InvoiceResponseDTO> morosos = billingService.listarCuotasMorosas(tenantId);
            
            if (morosos.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204 si la caja está al día y no hay morosos
            }
            return ResponseEntity.ok(morosos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener la lista de morosos");
        }
    }

    // ============================================================================
    // 3. ENDPOINT: HISTORIAL DE CUOTAS/DEUDAS DE UN MIEMBRO ESPECÍFICO
    // ============================================================================
    @GetMapping("/invoices/member/{memberId}")
    public ResponseEntity<?> listarHistorialMiembro(
            @RequestHeader("X-Tenant-Id") String tenantIdHeader, // 🔒 Inyectado automáticamente por el Gateway
            @PathVariable UUID memberId) {
        try {
            UUID tenantId = UUID.fromString(tenantIdHeader);
            List<InvoiceResponseDTO> historial = billingService.listarHistorialCuotasMiembro(tenantId, memberId);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener el historial financiero del miembro");
        }
    }
}