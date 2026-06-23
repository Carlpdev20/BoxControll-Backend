package com.proyecto.repository;

import com.proyecto.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {
    List<Member> findByTenantId(UUID tenantId);
    List<Member> findByTenantIdAndStatus(UUID tenantId, String status);
    Optional<Member> findByTenantIdAndId(UUID tenantId, UUID id);
    boolean existsByTenantIdAndDocumentNumber(UUID tenantId, String documentNumber);
    List<Member> findByTenantIdAndStatusNot(UUID tenantId, String status);
}