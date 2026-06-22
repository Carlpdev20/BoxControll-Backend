package com.proyecto.auth.controller;

import com.proyecto.auth.dto.LoginDTO;
import com.proyecto.auth.dto.RegistroAdminDTO;
import com.proyecto.auth.model.TenantAdmin;
import com.proyecto.auth.security.JwtUtil; // 👈 IMPORTAMOS EL UTILITARIO
import com.proyecto.auth.service.AuthService; // 👈 Nombre de clase corregido
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

            // CORREGIDO: Pasamos 3 parámetros para meter el ID del usuario dentro de los claims del JWT
            String token = jwtUtil.generateToken(admin.getEmail(), admin.getTenantId(), admin.getId());

            // Estructura JSON limpia para el almacenamiento en el Frontend (Angular)
            Map<String, Object> response = new HashMap<>();
            response.put("id", admin.getId()); //Anadido el UUID
            response.put("tenant_id", admin.getTenantId());
            response.put("token", token);

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
    
    @GetMapping("/profile/{id}")
    public ResponseEntity<?> obtenerPerfil(
            @RequestHeader("X-Tenant-Id") String tenantIdHeader,
            @PathVariable UUID id) {
        try {
            UUID tenantId = UUID.fromString(tenantIdHeader);
            
            // Buscamos el admin delegando la validación cruzada al Service
            TenantAdmin admin = authService.buscarPorTenantYId(tenantId, id);
            
            // Retornamos un payload limpio (evitando mandar la contraseña encriptada)
            Map<String, Object> perfil = new HashMap<>();
            perfil.put("id", admin.getId());
            perfil.put("firstName", admin.getFirstName());
            perfil.put("lastName", admin.getLastName());
            perfil.put("email", admin.getEmail());
            perfil.put("tenantId", admin.getTenantId());
            
            return ResponseEntity.ok(perfil);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Formato de cabeceras inválido.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}