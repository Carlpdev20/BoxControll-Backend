package com.proyecto.service;

import com.proyecto.model.Attendance;
import com.proyecto.model.Member;
import com.proyecto.repository.AttendanceRepository;
import com.proyecto.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final MemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public Attendance procesarCheckIn(UUID tenantId, String documentNumber) {
        Attendance log = new Attendance();
        log.setTenantId(tenantId);
        log.setDocumentNumber(documentNumber);

        // 1. Buscar si el miembro existe en este Tenant
        Optional<Member> memberOpt = memberRepository.findByTenantIdAndDocumentNumber(tenantId, documentNumber);

        if (memberOpt.isEmpty()) {
            log.setMemberName("Desconocido");
            log.setAllowed(false);
            log.setMessage("El documento no está registrado");
            return attendanceRepository.save(log);
        }

        Member m = memberOpt.get();
        log.setMemberName(m.getFirstName() + " " + m.getLastName());

        // 2. Validar Estado e Integridad de Fecha
        boolean estaActivo = "active".equalsIgnoreCase(m.getStatus());
        boolean noHaVencido = m.getExpiresAt() != null && m.getExpiresAt().isAfter(LocalDateTime.now());

        if (!estaActivo) {
            log.setAllowed(false);
            log.setMessage("Usuario Inactivo / Suspendido");
        } else if (!noHaVencido) {
            log.setAllowed(false);
            log.setMessage("Membresía Expirada (Venció: " + m.getExpiresAt().toLocalDate() + ")");
        } else {
            // ¡Todo en orden! Pass libre
            log.setAllowed(true);
            log.setMessage("Acceso Permitido");
        }

        return attendanceRepository.save(log);
    }

    public List<Attendance> listarHistorial(UUID tenantId) {
        return attendanceRepository.findByTenantIdOrderByAccessTimeDesc(tenantId);
    }
}