package com.proyecto.billing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "invoice_number"})
})
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId; // 🔒 Aislamiento SaaS Multi-tenant [cite: 27]

    @Column(name = "membership_id", nullable = false)
    private UUID membershipId; // Referencia lógica al contrato (de member-service)

    @Column(name = "member_id", nullable = false)
    private UUID memberId; // Referencia lógica al deportista (de member-service)

    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber; // Ej: FACT-2026-0001

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // Monto que DEBE pagar en esta cuota

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate; // Fecha de vencimiento de la cuota

    @Column(length = 20)
    private String status = "unpaid"; // 'unpaid', 'paid', 'overdue'

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}