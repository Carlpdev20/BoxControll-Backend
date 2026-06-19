package com.proyecto.auth.controller;

import com.proyecto.auth.dto.LoginDTO;
import com.proyecto.auth.dto.RegistroAdminDTO;
import com.proyecto.auth.model.TenantAdmin;
import com.proyecto.auth.security.JwtUtil; // 👈 IMPORTAMOS EL UTILITARIO
import com.proyecto.auth.service.AuthService; // 👈 Nombre de clase corregido
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService; // 👈 Sincronizado con el nuevo nombre

    @Autowired
    private JwtUtil jwtUtil; // 👈 INYECTAMOS EL GENERADOR DE TOKENS REAL

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginRequest) {
        try {
            // Invoca la validación real en PostgreSQL
            TenantAdmin admin = authService.login(loginRequest);
            
            // GENERACIÓN REAL DEL TOKEN JWT MULTI-TENANT
            String token = jwtUtil.generateToken(admin.getEmail(), admin.getTenantId());
            
            // Estructura JSON limpia para el almacenamiento en el Frontend (Angular)
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("tenant_id", admin.getTenantId());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            // Captura de "Usuario no encontrado" o "Contraseña incorrecta"
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistroAdminDTO registroRequest) {
        try {
            TenantAdmin nuevoAdmin = authService.registrarAdmin(registroRequest);
            return ResponseEntity.ok(nuevoAdmin);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno del servidor");
        }
    }
}