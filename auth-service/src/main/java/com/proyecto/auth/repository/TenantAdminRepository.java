package com.proyecto.auth.repository;

import com.proyecto.auth.model.TenantAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantAdminRepository extends JpaRepository<TenantAdmin, UUID> {
	
	Optional<TenantAdmin> findByEmail(String email);
	Optional<TenantAdmin> findByTenantIdAndEmail(UUID tenantId, String email);
	Optional<TenantAdmin> findByTenantIdAndId(UUID tenantId, UUID id);
}