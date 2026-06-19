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
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId; // 🔒 Aislamiento SaaS Multi-tenant [cite: 27]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice; // Relación de objeto interna: vincula a la cuota que mata. Puede ser NULL si es pago directo.

    @Column(name = "membership_id", nullable = false)
    private UUID membershipId; // Referencia cruzada al contrato administrativo 

    @Column(name = "member_id", nullable = false)
    private UUID memberId; // Referencia cruzada al miembro que paga 

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // Monto efectivamente pagado 

    @Column(length = 3)
    private String currency = "PEN"; // Moneda por defecto Soles 

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod; // 'cash', 'transfer' [cite: 17, 54]

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate; // Fecha efectiva del cobro en caja 

    @Column(name = "reference_number", length = 100)
    private String referenceNumber; // Código de operación de transferencias bancarias [cite: 19, 54]

    @Column(length = 20)
    private String status = "completed"; // 'completed', 'pending', 'failed' [cite: 56]

    @Column(columnDefinition = "TEXT")
    private String notes;

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