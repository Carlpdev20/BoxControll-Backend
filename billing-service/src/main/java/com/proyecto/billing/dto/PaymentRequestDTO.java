package com.proyecto.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class PaymentRequestDTO {
    private UUID invoiceId;       // Puede ser NULL si es un pago directo sin cuota programada
    private UUID membershipId;    // Contrato de afiliación que se está pagando [cite: 54]
    private UUID memberId;        // ID del deportista que realiza el desembolso [cite: 54]
    private BigDecimal amount;    // Monto real de dinero que entrega el cliente [cite: 54]
    private String currency;      // Tipo de moneda (ej: 'PEN')
    private String paymentMethod; // Tipo de pago: 'cash' o 'transfer' 
    private LocalDate paymentDate;// Fecha en la que se realiza la transacción [cite: 54]
    private String referenceNumber;// Obligatorio si el método es 'transfer' para el extracto bancario 
    private String notes;         // Comentarios u observaciones adicionales de la caja
}