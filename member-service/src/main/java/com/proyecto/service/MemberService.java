package com.proyecto.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.dto.MemberDTO;
import com.proyecto.model.Member;
import com.proyecto.model.PlanType;

import java.time.LocalDateTime;
import com.proyecto.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class MemberService {

    private final MemberRepository memberRepository;

    public List<Member> listarPorTenant(UUID tenantId) {
    	return memberRepository.findByTenantId(tenantId);    }

    // 🔒 Modificado para asegurar que el ID pertenezca al Tenant autenticado
    public Member buscarPorId(UUID tenantId, UUID id) {
        return memberRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado en este gimnasio"));
    }

    @Transactional
    public Member crear(MemberDTO dto) {
        // 1. 🔥 Regla de negocio: Validar si el DNI ya existe en ESTE gimnasio antes de guardar
        if (memberRepository.existsByTenantIdAndDocumentNumber(dto.getTenantId(), dto.getDocumentNumber())) {
            throw new RuntimeException("El documento ya se encuentra registrado en este gimnasio");
        }

        Member m = new Member();
        
        // 2. 📝 Mapeo de datos básicos (¡Aquí reconectamos el DNI y el nombre!)
        m.setTenantId(dto.getTenantId()); 
        m.setFirstName(dto.getFirstName());
        m.setLastName(dto.getLastName());
        m.setEmail(dto.getEmail());
        m.setPhone(dto.getPhone());
        m.setDocumentType(dto.getDocumentType());
        m.setDocumentNumber(dto.getDocumentNumber()); // 👈 Asegura que no vaya NULL a Postgres
        m.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        
        // 3. 🧠 LÓGICA AUTOMÁTICA Y SEGURA DE SUSCRIPCIÓN
        if (dto.getPlan() != null && !dto.getPlan().isBlank()) {
            try {
                // Convertimos el String a Mayúsculas para buscarlo en el Enum ("Mensual" -> "MENSUAL")
                String planTexto = dto.getPlan().toUpperCase().trim();
                com.proyecto.model.PlanType tipoPlan = com.proyecto.model.PlanType.valueOf(planTexto);
                
                m.setPlan(tipoPlan.name()); // Guardamos "MENSUAL", "TRIMESTRAL" o "ANUAL"
                
                // Calculamos la fecha en base a los meses del Enum truncando los nanosegundos para Angular
                int mesesAsignados = tipoPlan.getMonths();
                m.setExpiresAt(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).plusMonths(mesesAsignados));
                
            } catch (IllegalArgumentException e) {
                // 🛡️ Fallback: Si mandan un texto inesperado, asignamos MENSUAL por defecto
                m.setPlan("MENSUAL");
                m.setExpiresAt(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).plusMonths(1));
            }
        } else {
            // Si el plan viene vacío desde el Front
            m.setPlan("MENSUAL");
            m.setExpiresAt(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).plusMonths(1));
        }
        
        return memberRepository.save(m);
    }

    @Transactional
    public Member actualizar(UUID tenantId, UUID id, MemberDTO dto) {
        // 1. Buscamos el miembro existente asegurando el aislamiento Multi-tenant
        // (Usa el método de tu repositorio, ya sea findById, findByTenantIdAndId, etc.)
        Member m = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado"));

        // Verificación de seguridad Tenant
        if (!m.getTenantId().equals(tenantId)) {
            throw new RuntimeException("No tiene permisos para modificar este miembro");
        }

        // 2. Actualizamos los datos blandos tradicionales
        m.setFirstName(dto.getFirstName());
        m.setLastName(dto.getLastName());
        m.setEmail(dto.getEmail());
        m.setPhone(dto.getPhone());
        m.setDocumentType(dto.getDocumentType());
        m.setDocumentNumber(dto.getDocumentNumber());
        m.setStatus(dto.getStatus() != null ? dto.getStatus() : m.getStatus());

        // 3. 🧠 RECALCULO EN CALIENTE DEL PLAN Y VENCIMIENTO
        if (dto.getPlan() != null && !dto.getPlan().isBlank()) {
            try {
                String planTexto = dto.getPlan().toUpperCase().trim();
                com.proyecto.model.PlanType tipoPlan = com.proyecto.model.PlanType.valueOf(planTexto);
                
                m.setPlan(tipoPlan.name()); // Guardamos "MENSUAL", "TRIMESTRAL" o "ANUAL"
                
                // Extendemos la membresía a partir de hoy truncando a segundos para Angular
                int mesesAsignados = tipoPlan.getMonths();
                m.setExpiresAt(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).plusMonths(mesesAsignados));
                
            } catch (IllegalArgumentException e) {
                // Fallback silencioso por si llega un texto corrupto
            }
        }

        return memberRepository.save(m);
    }
 // 📁 En tu MemberService.java

    @Transactional
    public Member conmutarEstado(UUID tenantId, UUID id) {
        // 1. Buscamos al miembro usando tu lógica existente
        Member m = buscarPorId(tenantId, id);

        // 2. Evaluamos y alternamos el estado (Toggle)
        if ("active".equalsIgnoreCase(m.getStatus())) {
            m.setStatus("inactive");
        } else {
            m.setStatus("active");
        }

        // 3. Guardamos y retornamos el registro modificado
        return memberRepository.save(m);
    }
}