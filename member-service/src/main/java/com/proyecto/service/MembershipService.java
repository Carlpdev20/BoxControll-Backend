package com.proyecto.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.dto.MembershipDTO;
import com.proyecto.member.client.BillingFeignClient;
import com.proyecto.model.Member;
import com.proyecto.model.Membership;
import com.proyecto.model.MembershipPlan;
import com.proyecto.repository.MemberRepository;
import com.proyecto.repository.MembershipPlanRepository;
import com.proyecto.repository.MembershipRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;
    private final BillingFeignClient billingFeignClient;

    public List<Membership> listarPorTenant(UUID tenantId) {
        return membershipRepository.findByTenantId(tenantId);
    }

    public Membership buscarPorId(UUID tenantId, UUID id) {
        // 🔒 Filtro compuesto obligatorio para evitar fugas de información entre gimnasios
        return membershipRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada en este gimnasio"));
    }

    @Transactional
    public Membership crear(MembershipDTO dto) {
        // 🔒 Verificaciones de seguridad existentes...
        Member member = memberRepository.findByTenantIdAndId(dto.getTenantId(), dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado en este gimnasio"));

        MembershipPlan plan = planRepository.findByTenantIdAndId(dto.getTenantId(), dto.getMembershipPlanId())
                .orElseThrow(() -> new RuntimeException("Plan comercial no encontrado en este gimnasio"));

        // 2. Persistencia local del contrato
        Membership ms = new Membership();
        ms.setTenantId(dto.getTenantId());
        ms.setMember(member);
        ms.setMembershipPlan(plan);
        ms.setAffiliationDate(dto.getAffiliationDate());
        ms.setStartDate(dto.getStartDate());
        ms.setEndDate(dto.getStartDate().plusDays(plan.getDurationDays()));
        ms.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        
        Membership membershipGuardada = membershipRepository.save(ms);

        // ============================================================================
        // 🔥 MICROSERVICIOS CON OPENFEIGN!
        // ============================================================================
        try {
            // Preparamos los datos financieros requeridos por la otra DB
            Map<String, Object> invoicePayload = new java.util.HashMap<>();
            invoicePayload.put("membershipId", membershipGuardada.getId());
            invoicePayload.put("memberId", member.getId());
            // Generamos un correlativo automatico para simular una factura real
            invoicePayload.put("invoiceNumber", "CUOTA-" + System.currentTimeMillis() % 100000);
            invoicePayload.put("amount", plan.getPrice()); // Monto extraído del catálogo comercial
            invoicePayload.put("dueDate", membershipGuardada.getStartDate().plusDays(7)); // 7 días de gracia para pagar

            // Disparo síncrono inter-servicio pasando el Tenant ID en el Header
            billingFeignClient.crearCuota(dto.getTenantId(), invoicePayload);
            
        } catch (Exception e) {
            // Si el billing-service está apagado, lanzamos excepción para hacer ROLLBACK 
            // del contrato y evitar inconsistencia de datos
            throw new RuntimeException("No se pudo procesar la suscripcion porque el modulo financiero no responde. Motivo: " + e.getMessage());
        }
        // ============================================================================

        return membershipGuardada;
    }

    @Transactional
    public Membership actualizar(UUID tenantId, UUID id, MembershipDTO dto) {
        Membership ms = buscarPorId(tenantId, id);
        
        ms.setAffiliationDate(dto.getAffiliationDate());
        ms.setStartDate(dto.getStartDate());
        
        // Recalculamos la fecha de fin por si se alteró la fecha de inicio en la edición
        if (ms.getMembershipPlan() != null) {
            ms.setEndDate(dto.getStartDate().plusDays(ms.getMembershipPlan().getDurationDays()));
        } else {
            ms.setEndDate(dto.getEndDate());
        }
        
        ms.setStatus(dto.getStatus());
        return membershipRepository.save(ms);
    }

    @Transactional
    public void cancelarMembresia(UUID tenantId, UUID id) {
    	System.out.println("DEBUG SAAS -> Buscando Tenant: " + tenantId + " | Membresia ID: " + id);
        // 🚫 Cambiado delete por cancelación lógica para resguardar la auditoría e historial del SaaS 
        Membership ms = buscarPorId(tenantId, id);
        ms.setStatus("cancelled");
        membershipRepository.save(ms);
    }
}