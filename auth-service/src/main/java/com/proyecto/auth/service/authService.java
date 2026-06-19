package com.proyecto.auth.service;

import com.proyecto.auth.dto.LoginDTO;
import com.proyecto.auth.dto.RegistroAdminDTO;
import com.proyecto.auth.model.TenantAdmin;
import com.proyecto.auth.repository.TenantAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	@Autowired
    private TenantAdminRepository repoAdmin;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // REGISTRO OPTIMIZADO PARA ENTORNOS SAAS MULTI-TENANT
    public TenantAdmin registrarAdmin(RegistroAdminDTO dto) {
        
        // 1. Validar unicidad del correo ÚNICAMENTE dentro del mismo gimnasio (Tenant)
        // Nota: Asegúrate de que tu interfaz TenantAdminRepository tenga declarado el método:
        // Optional<TenantAdmin> findByTenantIdAndEmail(UUID tenantId, String email);
        if (repoAdmin.findByTenantIdAndEmail(dto.tenantId, dto.email).isPresent()) { // 🔒 Regla del caso de uso
            throw new RuntimeException("El correo electrónico ya está registrado en este gimnasio");
        }

        // 2. Mapear DTO a Entidad
        TenantAdmin admin = new TenantAdmin();
        admin.setFirstName(dto.firstName);
        admin.setLastName(dto.lastName);
        admin.setEmail(dto.email);
        admin.setTenantId(dto.tenantId);
        admin.setActive(true);

        // 3. Encriptación hash segura con BCrypt
        admin.setPasswordHash(passwordEncoder.encode(dto.password));

        // 4. Persistencia en la tabla tenant_admins
        return repoAdmin.save(admin);
    }

    // LOGIN DE USUARIOS
    public TenantAdmin login(LoginDTO dto) {
        // 1. Buscar el usuario por email
        // Nota técnica: Si un usuario maneja cuentas en distintos gimnasios con el mismo email,
        // lo ideal a futuro es que el Login reciba también el 'slug' o 'tenantId' para aislarlo.
        TenantAdmin admin = repoAdmin.findByEmail(dto.email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Comparación segura de hashes (Clave limpia vs Hash de la BD)
        if (!passwordEncoder.matches(dto.password, admin.getPasswordHash())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return admin;
    }
}