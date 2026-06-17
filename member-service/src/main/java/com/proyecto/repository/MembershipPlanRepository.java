package com.proyecto.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.model.MembershipPlan;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, UUID> {
    List<MembershipPlan> findByTenantId(UUID tenantId);
    List<MembershipPlan> findByTenantIdAndActiveTrue(UUID tenantId);
    Optional<MembershipPlan> findByTenantIdAndId(UUID tenantId, UUID id);
}