package com.proyecto.auth.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.proyecto.auth.model.TenantAdmin;
import com.proyecto.auth.repository.TenantAdminRepository;
import com.proyecto.auth.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private TenantAdminRepository tenantAdminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String email = body.get("usuario"); // Mantenemos "usuario" en el JSON para no romper tu frontend/Postman
        String clave = body.get("clave");

        // 1. Buscar al administrador por email
        TenantAdmin admin = tenantAdminRepository.findByEmail(email).orElse(null);

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "El usuario no se encuentra registrado"));
        }

        // 2. Verificar si el usuario está activo
        if (Boolean.FALSE.equals(admin.getActive())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "El usuario se encuentra inactivo"));
        }

        // 3. Validar la contraseña usando BCrypt (compara texto plano vs hash de la BD)
        if (passwordEncoder.matches(clave, admin.getPasswordHash())) {
            
            // 4. Generar el token inyectando el Email y el Tenant ID (Aislamiento SaaS)
            String token = jwtUtil.generateToken(admin.getEmail(), admin.getTenantId().toString());
            
            return ResponseEntity.ok(Map.of("token", token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Contraseña incorrecta"));
    }
}