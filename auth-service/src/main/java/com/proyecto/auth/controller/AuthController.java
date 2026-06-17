package com.proyecto.auth.controller;

import com.proyecto.auth.dto.LoginDTO;
import com.proyecto.auth.dto.RegistroAdminDTO;
import com.proyecto.auth.model.TenantAdmin;
import com.proyecto.auth.service.authService;
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
    private authService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginRequest) {
        try {
            // Invoca la validación real de la BD
            TenantAdmin admin = authService.login(loginRequest);
            
            // Aquí puedes integrar tu generador de JWT existente. 
            // Por ahora devolvemos un formato JSON real con los datos para Angular:
            Map<String, Object> response = new HashMap<>();
            response.put("token", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI" + admin.getEmail());
            response.put("tenant_id", admin.getTenantId());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            // Si el servicio lanza "Usuario no encontrado" o "Contraseña incorrecta"
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistroAdminDTO registroRequest) {
        try {
            TenantAdmin nuevoAdmin = authService.registrarAdmin(registroRequest);
            return ResponseEntity.ok(nuevoAdmin);
        } catch (RuntimeException e) {
            // Captura si el correo ya existe
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno del servidor");
        }
    }
}