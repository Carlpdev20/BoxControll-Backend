package com.proyecto.service;

import com.proyecto.dto.MembershipDTO;
import com.proyecto.model.Member;
import com.proyecto.model.Membership;
import com.proyecto.model.MembershipPlan;
import com.proyecto.repository.MemberRepository;
import com.proyecto.repository.MembershipPlanRepository;
import com.proyecto.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;

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
        // 🔒 Verificamos que el miembro exista y sea propiedad de este tenant
        Member member = memberRepository.findByTenantIdAndId(dto.getTenantId(), dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado en este gimnasio"));

        // 🔒 Verificamos que el plan exista y sea propiedad de este tenant
        MembershipPlan plan = planRepository.findByTenantIdAndId(dto.getTenantId(), dto.getMembershipPlanId())
                .orElseThrow(() -> new RuntimeException("Plan comercial no encontrado en este gimnasio"));

        Membership ms = new Membership();
        ms.setTenantId(dto.getTenantId()); // Uso del campo plano optimizado
        ms.setMember(member);
        ms.setMembershipPlan(plan);
        ms.setAffiliationDate(dto.getAffiliationDate());
        ms.setStartDate(dto.getStartDate());
        
        // ✨ Se conserva tu cálculo automático basado en los días de duración del plan 
        ms.setEndDate(dto.getStartDate().plusDays(plan.getDurationDays()));
        ms.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        
        return membershipRepository.save(ms);
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
        // 🚫 Cambiado delete por cancelación lógica para resguardar la auditoría e historial del SaaS 
        Membership ms = buscarPorId(tenantId, id);
        ms.setStatus("cancelled");
        membershipRepository.save(ms);
    }
}