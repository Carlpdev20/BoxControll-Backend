package com.proyecto.service;

import com.proyecto.dto.MembershipPlanDTO;
import com.proyecto.model.MembershipPlan;
import com.proyecto.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipPlanService {

    private final MembershipPlanRepository planRepository;

    public List<MembershipPlan> listarPorTenant(UUID tenantId) {
        return planRepository.findByTenantId(tenantId);
    }

    public MembershipPlan buscarPorId(UUID tenantId, UUID id) {
        return planRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado en este gimnasio"));
    }

    @Transactional
    public MembershipPlan crear(MembershipPlanDTO dto) {
        MembershipPlan plan = new MembershipPlan();
        plan.setTenantId(dto.getTenantId());
        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription());
        plan.setDurationDays(dto.getDurationDays());
        plan.setPrice(dto.getPrice());
        plan.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "PEN");
        plan.setActive(dto.getActive() != null ? dto.getActive() : true);
        
        return planRepository.save(plan);
    }

    @Transactional
    public MembershipPlan actualizar(UUID tenantId, UUID id, MembershipPlanDTO dto) {
        MembershipPlan plan = buscarPorId(tenantId, id);
        
        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription());
        plan.setDurationDays(dto.getDurationDays());
        plan.setPrice(dto.getPrice());
        plan.setCurrency(dto.getCurrency());
        plan.setActive(dto.getActive());
        
        return planRepository.save(plan);
    }

    @Transactional
    public void desactivarPlan(UUID tenantId, UUID id) {
        // 🚫 Evitamos romper registros históricos de membresías vendidas haciendo una desactivación lógica
        MembershipPlan plan = buscarPorId(tenantId, id);
        plan.setActive(false);
        planRepository.save(plan);
    }
}