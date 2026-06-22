package com.proyecto.auth.service;

import com.proyecto.auth.dto.LoginDTO;
import com.proyecto.auth.dto.RegistroAdminDTO;
import com.proyecto.auth.model.TenantAdmin;
import com.proyecto.auth.repository.TenantAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private TenantAdminRepository repoAdmin;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // REGISTRO OPTIMIZADO PARA ENTORNOS SAAS MULTI-TENANT
    public TenantAdmin registrarAdmin(RegistroAdminDTO dto) {
    	// ✅ CORREGIDO: El primer parámetro es el UUID (tenantId) y el segundo es el String (email)
    	if (repoAdmin.findByTenantIdAndEmail(dto.tenantId, dto.email).isPresent()) { 
            throw new RuntimeException("El correo electronico ya esta registrado en este gimnasio");
        }

        TenantAdmin admin = new TenantAdmin();
        admin.setFirstName(dto.firstName);
        admin.setLastName(dto.lastName);
        admin.setEmail(dto.email);
        admin.setTenantId(dto.tenantId);
        admin.setActive(true);

        admin.setPasswordHash(passwordEncoder.encode(dto.password));

        return repoAdmin.save(admin);
    }

    // LOGIN DE USUARIOS
    public TenantAdmin login(LoginDTO dto) {
        TenantAdmin admin = repoAdmin.findByEmail(dto.email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.password, admin.getPasswordHash())) {
            throw new RuntimeException("Contrasena incorrecta");
        }

        return admin;
    }

    // ============================================================================
    // ✨ NUEVO MÉTODO: BUSQUEDA COMPUESTA SEGURA PARA PERFIL
    // ============================================================================
    public TenantAdmin buscarPorTenantYId(UUID tenantId, UUID id) {
        // Asegura el aislamiento: el admin buscado debe coincidir estrictamente con el tenant del token
        return repoAdmin.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado en este gimnasio"));
    }
}