package com.proyecto.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class InvoiceResponseDTO {
    private UUID id;
    private UUID membershipId;
    private UUID memberId;
    private String invoiceNumber; // Ej: FACT-2026-0001
    private BigDecimal amount;    // Monto que debe pagar en esta cuota mensual
    private LocalDate dueDate;    // Fecha límite de pago de la cuota
    private String status;        // 'unpaid', 'paid', 'overdue'
}