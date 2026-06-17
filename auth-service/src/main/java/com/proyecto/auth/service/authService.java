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
public class authService {

    @Autowired
    private TenantAdminRepository repoAdmin;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // REGISTRO REAL EN BASE DE DATOS
    public TenantAdmin registrarAdmin(RegistroAdminDTO dto) {
        // 1. Validar que el email no exista en PostgreSQL
        if (repoAdmin.findByEmail(dto.email).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        // 2. Mapear los datos del DTO a la Entidad Real
        TenantAdmin admin = new TenantAdmin();
        admin.setFirstName(dto.firstName);
        admin.setLastName(dto.lastName);
        admin.setEmail(dto.email);
        admin.setTenantId(dto.tenantId);
        admin.setActive(true);

        // 3. Encriptar la contraseña usando BCrypt antes de guardar
        admin.setPasswordHash(passwordEncoder.encode(dto.password));

        // 4. Guardar en la base de datos
        return repoAdmin.save(admin);
    }

    // LOGIN REAL CON VALIDACIÓN DE CONTRASEÑA ENCRIPTADA
    public TenantAdmin login(LoginDTO dto) {
        // 1. Buscar el usuario por email
        TenantAdmin admin = repoAdmin.findByEmail(dto.email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Comparar la clave limpia del DTO con el hash encriptado de la BD
        if (!passwordEncoder.matches(dto.password, admin.getPasswordHash())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return admin;
    }
}