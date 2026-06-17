package com.proyecto.service;

import com.proyecto.dto.TenantDTO;
import com.proyecto.model.Tenant;
import com.proyecto.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public List<Tenant> listarTodos() {
        return tenantRepository.findAll();
    }

    public Tenant buscarPorId(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gimnasio (Tenant) no encontrado"));
    }

    @Transactional
    public Tenant crear(TenantDTO dto) {
        // 🔥 Regla de negocio SaaS: Validar que la URL amigable (slug) no esté ocupada por otra empresa
        if (tenantRepository.existsBySlug(dto.getSlug())) {
            throw new RuntimeException("El identificador URL (slug) ya se encuentra registrado por otro gimnasio");
        }

        Tenant t = new Tenant();
        t.setName(dto.getName());
        t.setSlug(dto.getSlug());
        t.setEmail(dto.getEmail());
        t.setPhone(dto.getPhone());
        t.setActive(dto.getActive() != null ? dto.getActive() : true);
        
        return tenantRepository.save(t);
    }

    @Transactional
    public Tenant actualizar(UUID id, TenantDTO dto) {
        Tenant t = buscarPorId(id);
        
        // Si intenta cambiar el slug, validamos que no colisione con terceros
        if (!t.getSlug().equals(dto.getSlug()) && tenantRepository.existsBySlug(dto.getSlug())) {
            throw new RuntimeException("El nuevo slug ya está en uso");
        }
        
        t.setName(dto.getName());
        t.setSlug(dto.getSlug());
        t.setEmail(dto.getEmail());
        t.setPhone(dto.getPhone());
        t.setActive(dto.getActive());
        
        return tenantRepository.save(t);
    }

    @Transactional
    public void desactivarTenant(UUID id) {
        // 🚫 JAMÁS elimines físicamente un Tenant. Si lo haces, borrarás toda la información histórica 
        // de miembros, pagos y planes vinculados por clave foránea. Simplemente se le suspende el acceso.
        Tenant t = buscarPorId(id);
        t.setActive(false);
        tenantRepository.save(t);
    }
}