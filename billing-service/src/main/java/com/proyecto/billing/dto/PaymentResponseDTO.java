package com.proyecto.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class PaymentResponseDTO {
    private UUID id;
    private UUID invoiceId;
    private String invoiceNumber;   // Para confirmar qué cuota se acaba de liquidar
    private BigDecimal amount;      // Monto cobrado
    private String paymentMethod;   // 'cash' o 'transfer' 
    private LocalDate paymentDate;  // Fecha del cobro [cite: 54]
    private String referenceNumber; // Código de operación bancaria verificado 
    private String status;          // Estado final: 'completed' 
    private UUID memberId;
}