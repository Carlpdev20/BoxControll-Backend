package com.proyecto.billing.repository;

import com.proyecto.billing.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    // 🔒 Buscar una cuota específica por su numeración correlativa dentro de un gimnasio
    Optional<Invoice> findByTenantIdAndInvoiceNumber(UUID tenantId, String invoiceNumber);

    // 🔒 Listar todas las cuotas de un miembro (historial de deudas/pagos)
    List<Invoice> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);

    // 🔒 Listar cuotas por estado (ej: 'unpaid', 'paid') para reportes financieros
    List<Invoice> findByTenantIdAndStatus(UUID tenantId, String status);

    // 🔥 CONSULTA CRÍTICA PARA MOROSOS:
    // Busca cuotas que estén pendientes ('unpaid') y cuya fecha de vencimiento sea ANTES de la fecha actual.
    List<Invoice> findByTenantIdAndStatusAndDueDateBefore(UUID tenantId, String status, LocalDate currentDate);

    // 🔒 Listar las cuotas asociadas a un contrato/membresía específico
    List<Invoice> findByTenantIdAndMembershipId(UUID tenantId, UUID membershipId);
}