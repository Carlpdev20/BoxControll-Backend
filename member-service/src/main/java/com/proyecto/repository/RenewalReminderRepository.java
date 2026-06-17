package com.proyecto.repository;

import com.proyecto.model.RenewalReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RenewalReminderRepository extends JpaRepository<RenewalReminder, UUID> {
    List<RenewalReminder> findByTenantId(UUID tenantId);
    List<RenewalReminder> findByMembershipId(UUID membershipId);
    List<RenewalReminder> findByTenantIdAndSentFalse(UUID tenantId);
}