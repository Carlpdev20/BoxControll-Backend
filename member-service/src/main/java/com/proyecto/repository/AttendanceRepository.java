package com.proyecto.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.model.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    // Para mostrar el historial en el Front ordenado por el más reciente
    List<Attendance> findByTenantIdOrderByAccessTimeDesc(UUID tenantId);

}
