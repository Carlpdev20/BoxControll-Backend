package com.proyecto.service;

import com.proyecto.dto.MemberDTO;
import com.proyecto.model.Member;
import com.proyecto.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor 
public class MemberService {

    private final MemberRepository memberRepository;

    public List<Member> listarPorTenant(UUID tenantId) {
        return memberRepository.findByTenantId(tenantId);
    }

    // 🔒 Modificado para asegurar que el ID pertenezca al Tenant autenticado
    public Member buscarPorId(UUID tenantId, UUID id) {
        return memberRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado en este gimnasio"));
    }

    @Transactional
    public Member crear(MemberDTO dto) {
        // 🔥 Regla de negocio: Validar si el DNI ya existe en ESTE gimnasio antes de guardar
        if (memberRepository.existsByTenantIdAndDocumentNumber(dto.getTenantId(), dto.getDocumentNumber())) {
            throw new RuntimeException("El documento ya se encuentra registrado en este gimnasio");
        }

        Member m = new Member();
        m.setTenantId(dto.getTenantId()); // Asignación directa del UUID plano optimizado
        m.setFirstName(dto.getFirstName());
        m.setLastName(dto.getLastName());
        m.setEmail(dto.getEmail());
        m.setPhone(dto.getPhone());
        m.setDocumentType(dto.getDocumentType());
        m.setDocumentNumber(dto.getDocumentNumber());
        m.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        
        return memberRepository.save(m);
    }

    @Transactional
    public Member actualizar(UUID tenantId, UUID id, MemberDTO dto) {
        Member m = buscarPorId(tenantId, id); // Valida propiedad antes de editar
        
        m.setFirstName(dto.getFirstName());
        m.setLastName(dto.getLastName());
        m.setEmail(dto.getEmail());
        m.setPhone(dto.getPhone());
        m.setDocumentType(dto.getDocumentType());
        m.setDocumentNumber(dto.getDocumentNumber());
        m.setStatus(dto.getStatus());
        // 💡 Eliminamos m.setUpdatedAt() porque @UpdateTimestamp de Hibernate se encarga solo
        
        return memberRepository.save(m);
    }

    @Transactional
    public void eliminarLogico(UUID tenantId, UUID id) {
        // 🚫 Cambiado deleteById por eliminación lógica para resguardar la integridad referencial
        Member m = buscarPorId(tenantId, id);
        m.setStatus("inactive");
        memberRepository.save(m);
    }
}