package com.proyecto.billing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.billing.model.PaymentMethod;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID>{

	// 🔒 Listar todos los métodos de pago de un cliente asegurando el Tenant
    List<PaymentMethod> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);

    // 🔒 Obtener el método de pago por defecto/principal de un miembro
    Optional<PaymentMethod> findByTenantIdAndMemberIdAndIsDefaultTrue(UUID tenantId, UUID memberId);
}
