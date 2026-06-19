package com.proyecto.billing.service;

import com.proyecto.billing.dto.InvoiceRequestDTO;
import com.proyecto.billing.dto.InvoiceResponseDTO;
import com.proyecto.billing.dto.PaymentRequestDTO;
import com.proyecto.billing.dto.PaymentResponseDTO;
import com.proyecto.billing.model.Invoice;
import com.proyecto.billing.model.Payment;
import com.proyecto.billing.repository.InvoiceRepository;
import com.proyecto.billing.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BillingService {

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private InvoiceRepository invoiceRepo;

    // ============================================================================
    // 1. REGISTRAR PAGO Y LIQUIDAR CUOTA (TRANSACCIONAL)
    // ============================================================================
    @Transactional
    public PaymentResponseDTO registrarPago(UUID tenantId, PaymentRequestDTO dto) {
        
        Invoice invoice = null;

        // 1. Si el pago viene amarrado a una cuota (Invoice), la validamos y actualizamos
        if (dto.getInvoiceId() != null) {
            invoice = invoiceRepo.findById(dto.getInvoiceId())
                    .orElseThrow(() -> new RuntimeException("Cuota (Invoice) no encontrada"));
            
            // Verificación de seguridad Multi-tenant
            if (!invoice.getTenantId().equals(tenantId)) {
                throw new RuntimeException("Acceso denegado: La cuota no pertenece a este gimnasio");
            }

            // Cambiar el estado de la cuota a pagada
            invoice.setStatus("paid");
            invoiceRepo.save(invoice);
        }

        // 2. Validación de regla de negocio: Si es transferencia, exige número de referencia
        if ("transfer".equalsIgnoreCase(dto.getPaymentMethod()) && 
           (dto.getReferenceNumber() == null || dto.getReferenceNumber().trim().isEmpty())) {
            throw new RuntimeException("Las transferencias bancarias requieren obligatoriamente un número de referencia");
        }

        // 3. Mapear DTO a la Entidad Real de Pagos
        Payment payment = new Payment();
        payment.setTenantId(tenantId); // 🔒 Asignamos el Tenant del administrador actual
        payment.setInvoice(invoice);   // Vinculación física de objetos
        payment.setMembershipId(dto.getMembershipId());
        payment.setMemberId(dto.getMemberId());
        payment.setAmount(dto.getAmount());
        payment.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "PEN");
        payment.setPaymentMethod(dto.getPaymentMethod().toLowerCase());
        payment.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDate.now());
        payment.setReferenceNumber(dto.getReferenceNumber());
        payment.setNotes(dto.getNotes());
        payment.setStatus("completed");

        // 4. Guardar el pago en PostgreSQL
        Payment paymentGuardado = paymentRepo.save(payment);

        // 5. Construir el DTO de respuesta para el Frontend
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setId(paymentGuardado.getId());
        response.setInvoiceId(invoice != null ? invoice.getId() : null);
        response.setInvoiceNumber(invoice != null ? invoice.getInvoiceNumber() : "PAGO_DIRECTO");
        response.setAmount(paymentGuardado.getAmount());
        response.setPaymentMethod(paymentGuardado.getPaymentMethod());
        response.setPaymentDate(paymentGuardado.getPaymentDate());
        response.setReferenceNumber(paymentGuardado.getReferenceNumber());
        response.setStatus(paymentGuardado.getStatus());

        return response;
    }

    // ============================================================================
    // 2. OBTENER LISTA DE MOROSOS AUTOMÁTICA
    // ============================================================================
    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> listarCuotasMorosas(UUID tenantId) {
        // Busca las cuotas 'unpaid' cuyo due_date ya pasó con respecto a HOY
        List<Invoice> morosas = invoiceRepo.findByTenantIdAndStatusAndDueDateBefore(
                tenantId, "unpaid", LocalDate.now());

        // Convertimos la lista de entidades a DTOs limpios para Angular
        return morosas.stream().map(this::mapearAInvoiceResponseDTO).collect(Collectors.toList());
    }

    // ============================================================================
    // 3. HISTORIAL DE CUOTAS DE UN MIEMBRO
    // ============================================================================
    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> listarHistorialCuotasMiembro(UUID tenantId, UUID memberId) {
        List<Invoice> historial = invoiceRepo.findByTenantIdAndMemberId(tenantId, memberId);
        return historial.stream().map(this::mapearAInvoiceResponseDTO).collect(Collectors.toList());
    }

    // ============================================================================
    // METODOS DE APOYO (MAPPERS)
    // ============================================================================
    private InvoiceResponseDTO mapearAInvoiceResponseDTO(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setId(invoice.getId());
        dto.setMembershipId(invoice.getMembershipId());
        dto.setMemberId(invoice.getMemberId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setAmount(invoice.getAmount());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getStatus());
        return dto;
    }
    
    @Transactional
    public InvoiceResponseDTO crearCuotaInmediata(UUID tenantId, InvoiceRequestDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setMembershipId(dto.getMembershipId());
        invoice.setMemberId(dto.getMemberId());
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setAmount(dto.getAmount());
        invoice.setDueDate(dto.getDueDate());
        invoice.setStatus("unpaid"); // Nace como deuda pendiente

        Invoice guardada = invoiceRepo.save(invoice);
        return mapearAInvoiceResponseDTO(guardada);
    }
}