package com.proyecto.billing.repository;

import com.proyecto.billing.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // 🔒 Listar todos los pagos reales que ingresaron a un gimnasio en un rango de fechas (Cierre de Caja)
    List<Payment> findByTenantIdAndPaymentDateBetween(UUID tenantId, LocalDate startDate, LocalDate endDate);

    // 🔍 CONCILIACIÓN BANCARIA INMEDIATA:
    // Permite al administrador buscar un pago exacto usando el número de operación del voucher de transferencia.
    Optional<Payment> findByTenantIdAndReferenceNumber(UUID tenantId, String referenceNumber);

    // 🔒 Listar todo el dinero que ha pagado históricamente un deportista específico
    List<Payment> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);
}
