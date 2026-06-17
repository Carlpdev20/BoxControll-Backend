package com.proyecto.repository;

import com.proyecto.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    List<Membership> findByTenantId(UUID tenantId);
    List<Membership> findByMemberId(UUID memberId);
    List<Membership> findByTenantIdAndStatus(UUID tenantId, String status);
    Optional<Membership> findByTenantIdAndId(UUID tenantId, UUID id);
}