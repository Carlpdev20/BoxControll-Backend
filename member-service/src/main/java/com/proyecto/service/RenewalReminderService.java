package com.proyecto.service;

import com.proyecto.dto.RenewalReminderDTO;
import com.proyecto.model.Member;
import com.proyecto.model.Membership;
import com.proyecto.model.RenewalReminder;
import com.proyecto.repository.MemberRepository;
import com.proyecto.repository.MembershipRepository;
import com.proyecto.repository.RenewalReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RenewalReminderService {

    private final RenewalReminderRepository reminderRepository;
    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;

    public List<RenewalReminder> listarPorTenant(UUID tenantId) {
        return reminderRepository.findByTenantId(tenantId);
    }

    public RenewalReminder buscarPorId(UUID tenantId, UUID id) {
        // 🔒 Implementación de seguridad multi-tenant básica para las alertas
        return reminderRepository.findById(id)
                .filter(reminder -> reminder.getTenantId().equals(tenantId))
                .orElseThrow(() -> new RuntimeException("Recordatorio no encontrado o no pertenece a su gimnasio"));
    }

    @Transactional
    public RenewalReminder crear(RenewalReminderDTO dto) {
        Membership membership = membershipRepository.findByTenantIdAndId(dto.getTenantId(), dto.getMembershipId())
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada en este gimnasio"));
                
        Member member = memberRepository.findByTenantIdAndId(dto.getTenantId(), dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado en este gimnasio"));

        RenewalReminder r = new RenewalReminder();
        r.setTenantId(dto.getTenantId());
        r.setMembership(membership);
        r.setMember(member);
        
        // ✨ Se conserva tu lógica nativa de automatizar la alerta 14 días antes del fin 
        r.setReminderDate(membership.getEndDate().minusDays(14));
        r.setSent(false);
        r.setContactMethod(dto.getContactMethod() != null ? dto.getContactMethod() : "whatsapp");
        
        return reminderRepository.save(r);
    }

    @Transactional
    public RenewalReminder marcarEnviado(UUID tenantId, UUID id) {
        RenewalReminder r = buscarPorId(tenantId, id);
        r.setSent(true); // Evita duplicar el mensaje al mismo deportista [cite: 60]
        return reminderRepository.save(r);
    }

    @Transactional
    public void eliminarRecordatorio(UUID tenantId, UUID id) {
        // Verificamos propiedad antes de eliminar físicamente de la bandeja de salida
        RenewalReminder r = buscarPorId(tenantId, id);
        reminderRepository.delete(r);
    }
}