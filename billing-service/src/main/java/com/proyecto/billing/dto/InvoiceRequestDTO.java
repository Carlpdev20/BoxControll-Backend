package com.proyecto.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class InvoiceRequestDTO {
    private UUID membershipId;
    private UUID memberId;
    private String invoiceNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
}