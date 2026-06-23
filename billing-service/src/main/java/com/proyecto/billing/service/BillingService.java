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
    private PaymentRepository paymentRepo; // 🎯 Nombre clave: paymentRepo

    @Autowired
    private InvoiceRepository invoiceRepo;

    // ============================================================================
    // 1. REGISTRAR PAGO Y LIQUIDAR CUOTA (TRANSACCIONAL)
    // ============================================================================
    @Transactional
    public PaymentResponseDTO registrarPago(UUID tenantId, PaymentRequestDTO dto) {
        
        Invoice invoice = null;

        if (dto.getInvoiceId() != null) {
            invoice = invoiceRepo.findById(dto.getInvoiceId())
                    .orElseThrow(() -> new RuntimeException("Cuota (Invoice) no encontrada"));
            
            if (!invoice.getTenantId().equals(tenantId)) {
                throw new RuntimeException("Acceso denegado: La cuota no pertenece a este gimnasio");
            }

            invoice.setStatus("paid");
            invoiceRepo.save(invoice);
        }

        if ("transfer".equalsIgnoreCase(dto.getPaymentMethod()) && 
           (dto.getReferenceNumber() == null || dto.getReferenceNumber().trim().isEmpty())) {
            throw new RuntimeException("Las transferencias bancarias requieren obligatoriamente un número de referencia");
        }

        Payment payment = new Payment();
        payment.setTenantId(tenantId);
        payment.setInvoice(invoice);   
        payment.setMembershipId(dto.getMembershipId());
        payment.setMemberId(dto.getMemberId());
        payment.setAmount(dto.getAmount());
        payment.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "PEN");
        payment.setPaymentMethod(dto.getPaymentMethod().toLowerCase());
        payment.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDate.now());
        payment.setReferenceNumber(dto.getReferenceNumber());
        payment.setNotes(dto.getNotes());
        payment.setStatus("completed");

        Payment paymentGuardado = paymentRepo.save(payment);

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
        // 🎯 CAMBIO: Quitamos el filtro de fecha anterior para permitir pagos adelantados
        List<Invoice> porCobrar = invoiceRepo.findByTenantIdAndStatus(tenantId, "unpaid");

        // Convertimos a DTOs limpios para Angular
        return porCobrar.stream().map(this::mapearAInvoiceResponseDTO).collect(Collectors.toList());
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
    // 4. HISTORIAL DE CAJA GLOBAL (MÉTODO CORREGIDO)
    // ============================================================================
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> listarTodosLosPagos(UUID tenantId) {
        // 🎯 CORREGIDO: Cambiado paymentRepository por paymentRepo para que compile sin problemas
        List<Payment> pagos = paymentRepo.findByTenantIdOrderByCreatedAtDesc(tenantId);
        
        return pagos.stream().map(p -> {
            PaymentResponseDTO dto = new PaymentResponseDTO();
            dto.setId(p.getId());
            dto.setInvoiceId(p.getInvoice() != null ? p.getInvoice().getId() : null);
            dto.setAmount(p.getAmount());
            dto.setPaymentDate(p.getPaymentDate());
            dto.setPaymentMethod(p.getPaymentMethod());
            dto.setReferenceNumber(p.getReferenceNumber());
            dto.setStatus(p.getStatus());
            dto.setMemberId(p.getMemberId()); 
            return dto;
        }).collect(Collectors.toList());
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
        invoice.setStatus("unpaid");

        Invoice guardada = invoiceRepo.save(invoice);
        return mapearAInvoiceResponseDTO(guardada);
    }
}